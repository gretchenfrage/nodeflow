package com.phoenixkahlo.nodenet.stream;

import static com.phoenixkahlo.nodenet.serialization.SerializationUtils.intToBytes;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.phoenixkahlo.util.EndableThread;
import com.phoenixkahlo.util.TimeWarningThread;
import com.phoenixkahlo.util.TriFunction;

public class BasicStreamFamily implements StreamFamily {

	private UDPSocketWrapper udpWrapper;
	// Synchronize usages
	private List<ChildStream> children = new ArrayList<>();
	private EndableThread receivingThread;
	private EndableThread heartbeatThread;
	private EndableThread retransmissionThread;
	// Usage of unconfirmedConnections should be synchronized, and it should be
	// notified upon removal.
	private List<Integer> unconfirmedConnections = new ArrayList<>();
	private Predicate<PotentialConnection> receiveTest;
	private Consumer<DatagramStream> receiveHandler;
	private TriFunction<StreamFamily, Integer, SocketAddress, ChildStream> childSocketFactory;
	
	private volatile boolean disconnected = false;

	public BasicStreamFamily(UDPSocketWrapper wrapper, EndableThread receivingThread, EndableThread heartbeatThread,
			EndableThread retransmissionThread,
			TriFunction<StreamFamily, Integer, SocketAddress, ChildStream> childSocketFactory) {
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

	public BasicStreamFamily(int port) throws SocketException {
		this.udpWrapper = new RealUDPSocketWrapper(port);
		receivingThread = new FamilyReceivingThread(this);
		heartbeatThread = new FamilyHeartbeatThread(this);
		retransmissionThread = new FamilyRetransmissionThread(this);
		this.childSocketFactory = BasicChildStream::new;
		disableReceiver();
		receivingThread.start();
		heartbeatThread.start();
		retransmissionThread.start();
	}

	public BasicStreamFamily() throws SocketException {
		this.udpWrapper = new RealUDPSocketWrapper();
		receivingThread = new FamilyReceivingThread(this);
		heartbeatThread = new FamilyHeartbeatThread(this);
		retransmissionThread = new FamilyRetransmissionThread(this);
		this.childSocketFactory = BasicChildStream::new;
		disableReceiver();
		receivingThread.start();
		heartbeatThread.start();
		retransmissionThread.start();
	}
	
	@Override
	public void setReceiveTest(Predicate<PotentialConnection> receiveTest) {
		this.receiveTest = receiveTest;
	}
	
	@Override
	public void setReceiveHandler(Consumer<DatagramStream> receiveHandler) {
		this.receiveHandler = receiveHandler;
	}

	@Override
	public Optional<DatagramStream> connect(InetSocketAddress address) {
		if (disconnected)
			return Optional.empty();
		
		int connectionID = ThreadLocalRandom.current().nextInt() & DatagramStreamConfig.CONNECTION_ID_RANGE;
		try {
			udpWrapper.send(intToBytes(connectionID | DatagramStreamConfig.CONNECT), address);
		} catch (IOException e1) {
			return Optional.empty();
		}

		synchronized (unconfirmedConnections) {
			unconfirmedConnections.add(connectionID);
			while (unconfirmedConnections.contains(connectionID)) {
				try {
					unconfirmedConnections.wait();
				} catch (InterruptedException e) {
					System.err.println("Interrupted while waiting on unconfirmedConnections");
					e.printStackTrace();
				}
			}
		}

		Optional<ChildStream> optional = children.stream().filter(child -> child.getConnectionID() == connectionID)
				.findAny();
		if (optional.isPresent())
			return Optional.of(optional.get());
		else
			return Optional.empty();
	}

	@Override
	public void receiveAccept(int connectionID, InetSocketAddress from) {
		synchronized (unconfirmedConnections) {
			if (!unconfirmedConnections.contains(connectionID)) {
				System.err.println("ACCEPT received with connectionID " + connectionID + " from " + from
						+ ", not a valid pending ID.");
				return;
			}
		}
		synchronized (children) {
			children.add(childSocketFactory.apply(this, connectionID, from));
		}
		synchronized (unconfirmedConnections) {
			unconfirmedConnections.removeIf(n -> n == connectionID);
			unconfirmedConnections.notifyAll();
		}

	}

	@Override
	public void receiveReject(int connectionID, InetSocketAddress from) {
		synchronized (unconfirmedConnections) {
			if (unconfirmedConnections.contains(connectionID)) {
				unconfirmedConnections.remove(connectionID);
				unconfirmedConnections.notifyAll();
			} else {
				System.err.println("REJECT received with connectionID " + connectionID + " from " + from
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
			for (ChildStream child : children) {
				child.disconnect();
			}
		}
		receivingThread.end();
		heartbeatThread.end();
		retransmissionThread.end();
	}

	@Override
	public void receiveConnect(int connectionID, InetSocketAddress from) {
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
				udpWrapper.send(intToBytes(connectionID | DatagramStreamConfig.ACCEPT), from);
			} catch (IOException e) {
				System.err.println("IOException while accepting connection");
				e.printStackTrace();
			}
			Thread handleTimer = new TimeWarningThread(
					"Warning: " + this + " receive handler taking long amount of time.", 50);
			receiveHandler.accept(socket);
			handleTimer.interrupt();
		} else {
			try {
				udpWrapper.send(intToBytes(connectionID | DatagramStreamConfig.REJECT), from);
			} catch (IOException e) {
				System.err.println("IOException while rejecting connection");
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public String toString() {
		return "BasicStreamFamily children=" + children;
	}

}
