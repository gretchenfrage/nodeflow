package com.phoenixkahlo.nodenet.stream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.phoenixkahlo.util.EndableThread;
import com.phoenixkahlo.util.TimeWarningThread;
import com.phoenixkahlo.util.TriFunction;
import com.phoenixkahlo.util.UUID;

public class BasicStreamFamily implements StreamFamily {

	private UDPSocketWrapper udpWrapper;
	// Synchronize usages
	private List<ChildStream> children = new ArrayList<>();
	private EndableThread receivingThread;
	private EndableThread heartbeatThread;
	private EndableThread retransmissionThread;
	// Usage of unconfirmedConnections should be synchronized, and it should be
	// notified upon removal.
	private List<UUID> unconfirmedConnections = new ArrayList<>();
	private Predicate<PotentialConnection> receiveTest;
	private Consumer<DatagramStream> receiveHandler;
	private TriFunction<StreamFamily, UUID, InetSocketAddress, ChildStream> childSocketFactory;
	
	private PrintStream err;
	
	private volatile boolean disconnected = false;

	public BasicStreamFamily(UDPSocketWrapper wrapper, EndableThread receivingThread, EndableThread heartbeatThread,
			EndableThread retransmissionThread,
			TriFunction<StreamFamily, UUID, InetSocketAddress, ChildStream> childSocketFactory) {
		this.udpWrapper = wrapper;
		this.receivingThread = receivingThread;
		this.heartbeatThread = heartbeatThread;
		this.retransmissionThread = retransmissionThread;
		this.childSocketFactory = childSocketFactory;
		disableReceiver();
		receivingThread.start();
		heartbeatThread.start();
		retransmissionThread.start();
	}

	public BasicStreamFamily(int port, PrintStream err) throws SocketException {
		this.udpWrapper = new RealUDPSocketWrapper(port);
		receivingThread = new FamilyReceivingThread(this, err);
		heartbeatThread = new FamilyHeartbeatThread(this);
		retransmissionThread = new FamilyRetransmissionThread(this);
		this.childSocketFactory = (family, id, address) -> new BasicChildStream(family, id, address, err);
		disableReceiver();
		receivingThread.start();
		heartbeatThread.start();
		retransmissionThread.start();
		
		this.err = err;
	}

	public BasicStreamFamily(PrintStream err) throws SocketException {
		this.udpWrapper = new RealUDPSocketWrapper();
		receivingThread = new FamilyReceivingThread(this, err);
		heartbeatThread = new FamilyHeartbeatThread(this);
		retransmissionThread = new FamilyRetransmissionThread(this);
		this.childSocketFactory = (family, id, address) -> new BasicChildStream(family, id, address, err);
		disableReceiver();
		receivingThread.start();
		heartbeatThread.start();
		retransmissionThread.start();
		
		this.err = err;
	}
	
	public BasicStreamFamily(int port) throws SocketException {
		this(port, System.err);
	}
	
	public BasicStreamFamily() throws SocketException {
		this(System.err);
	}
	
	@Override
	public void setReceiveTest(Predicate<PotentialConnection> receiveTest) {
		this.receiveTest = receiveTest;
	}
	
	@Override
	public void setReceiveHandler(Consumer<DatagramStream> receiveHandler, boolean launchNewThread) {
		if (launchNewThread)
			this.receiveHandler = stream -> new Thread(() -> receiveHandler.accept(stream)).start();
		else
			this.receiveHandler = receiveHandler;
	}

	@Override
	public Optional<DatagramStream> connect(InetSocketAddress address) {
		if (disconnected)
			return Optional.empty();
		
		//int connectionID = ThreadLocalRandom.current().nextInt() & DatagramStreamConfig.CONNECTION_ID_RANGE;
		UUID connectionID = new UUID();
		
		synchronized (unconfirmedConnections) {
			unconfirmedConnections.add(connectionID);
		}
		
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			baos.write(DatagramStreamConfig.CONNECT);
			connectionID.write(baos);
			udpWrapper.send(baos.toByteArray(), address);
			//udpWrapper.send(intToBytes(connectionID | DatagramStreamConfig.CONNECT), address);
		} catch (IOException e1) {
			return Optional.empty();
		}

		synchronized (unconfirmedConnections) {
			while (unconfirmedConnections.contains(connectionID)) {
				try {
					unconfirmedConnections.wait();
				} catch (InterruptedException e) {
					err.println("Interrupted while waiting on unconfirmedConnections");
					e.printStackTrace();
				}
			}
		}

		Optional<ChildStream> optional = children.stream().filter(child -> child.getConnectionID().equals(connectionID))
				.findAny();
		if (optional.isPresent())
			return Optional.of(optional.get());
		else
			return Optional.empty();
	}

	@Override
	public void receiveAccept(UUID connectionID, InetSocketAddress from) {
		synchronized (unconfirmedConnections) {
			if (!unconfirmedConnections.contains(connectionID)) {
				err.println("ACCEPT received with connectionID " + connectionID + " from " + from
						+ ", not a valid pending ID.");
				return;
			}
		}
		synchronized (children) {
			children.add(childSocketFactory.apply(this, connectionID, from));
		}
		synchronized (unconfirmedConnections) {
			unconfirmedConnections.removeIf(n -> n.equals(connectionID));
			unconfirmedConnections.notifyAll();
		}

	}

	@Override
	public void receiveReject(UUID connectionID, InetSocketAddress from) {
		synchronized (unconfirmedConnections) {
			if (unconfirmedConnections.contains(connectionID)) {
				unconfirmedConnections.remove(connectionID);
				unconfirmedConnections.notifyAll();
			} else {
				err.println("REJECT received with connectionID " + connectionID + " from " + from
						+ ", not a valid pending ID.");
			}
		}
	}

	@Override
	public List<ChildStream> getChildren() {
		return children;
	}

	@Override
	public UDPSocketWrapper getUDPWrapper() {
		return udpWrapper;
	}

	@Override
	public void close() {
		synchronized (children) {
			for (int i = children.size() - 1; i >= 0; i--) {
				children.get(i).disconnect();
			}
		}
		receivingThread.end();
		heartbeatThread.end();
		retransmissionThread.end();
	}

	@Override
	public void receiveConnect(UUID connectionID, InetSocketAddress from) {
		Thread acceptTimer = new TimeWarningThread("Warning: " + this + " receive test taking long amount of time.",
				50);
		boolean accept = receiveTest.test(new PotentialConnection(from));
		acceptTimer.interrupt();

		if (accept) {
			ChildStream socket = childSocketFactory.apply(this, connectionID, from);
			synchronized (children) {
				children.add(socket);
			}
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				baos.write(DatagramStreamConfig.ACCEPT);
				connectionID.write(baos);
				udpWrapper.send(baos.toByteArray(), from);
				//udpWrapper.send(intToBytes(connectionID | DatagramStreamConfig.ACCEPT), from);
			} catch (IOException e) {
				err.println("IOException while accepting connection");
				e.printStackTrace();
			}
			Thread handleTimer = new TimeWarningThread(
					"Warning: " + this + " receive handler taking long amount of time.", 50);
			receiveHandler.accept(socket);
			handleTimer.interrupt();
		} else {
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				baos.write(DatagramStreamConfig.REJECT);
				connectionID.write(baos);
				udpWrapper.send(baos.toByteArray(), from);
				//udpWrapper.send(intToBytes(connectionID | DatagramStreamConfig.REJECT), from);
			} catch (IOException e) {
				err.println("IOException while rejecting connection");
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public String toString() {
		return "BasicStreamFamily children=" + children;
	}

}
