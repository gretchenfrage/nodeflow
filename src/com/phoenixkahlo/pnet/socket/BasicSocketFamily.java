package com.phoenixkahlo.pnet.socket;

import static com.phoenixkahlo.pnet.serialization.SerializationUtils.intToBytes;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.phoenixkahlo.util.EndableThread;
import com.phoenixkahlo.util.TriFunction;

public class BasicSocketFamily implements SocketFamily {

	private Random random = new Random();
	private UDPSocketWrapper udpWrapper;
	// Synchronize usages
	private List<ChildSocket> children = new ArrayList<>();
	private EndableThread receivingThread;
	private EndableThread heartbeatThread;
	private EndableThread retransmissionThread;
	// Usage of unconfirmedConnections should be synchronized, and it should be
	// notified upon removal.
	private List<Integer> unconfirmedConnections = new ArrayList<>();
	private Predicate<PotentialSocketConnection> receiveTest;
	private Consumer<PNetSocket> receiveHandler;
	private TriFunction<SocketFamily, Integer, SocketAddress, ChildSocket> childSocketFactory;

	public BasicSocketFamily(UDPSocketWrapper wrapper, EndableThread receivingThread, EndableThread heartbeatThread,
			EndableThread retransmissionThread,
			TriFunction<SocketFamily, Integer, SocketAddress, ChildSocket> childSocketFactory) {
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

	public BasicSocketFamily(int port) throws SocketException {
		this.udpWrapper = new RealUDPSocketWrapper(port);
		receivingThread = new FamilyReceivingThread(this);
		heartbeatThread = new FamilyHeartbeatThread(this);
		retransmissionThread = new FamilyRetransmissionThread(this);
		this.childSocketFactory = BasicChildSocket::new;
		disableReceiver();
		receivingThread.start();
		heartbeatThread.start();
		retransmissionThread.start();
	}

	public BasicSocketFamily() throws SocketException {
		this.udpWrapper = new RealUDPSocketWrapper();
		receivingThread = new FamilyReceivingThread(this);
		heartbeatThread = new FamilyHeartbeatThread(this);
		retransmissionThread = new FamilyRetransmissionThread(this);
		this.childSocketFactory = BasicChildSocket::new;
		disableReceiver();
		receivingThread.start();
		heartbeatThread.start();
		retransmissionThread.start();
	}
	
	@Override
	public void setReceiver(Predicate<PotentialSocketConnection> receiveTest, Consumer<PNetSocket> receiveHandler) {
		this.receiveTest = receiveTest;
		this.receiveHandler = receiveHandler;
	}

	@Override
	public Optional<PNetSocket> connect(SocketAddress address) {
		int connectionID = random.nextInt() & SocketConstants.CONNECTION_ID_RANGE;
		try {
			udpWrapper.send(intToBytes(connectionID | SocketConstants.CONNECT), address);
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

		Optional<ChildSocket> optional = children.stream().filter(child -> child.getConnectionID() == connectionID)
				.findAny();
		if (optional.isPresent())
			return Optional.of(optional.get());
		else
			return Optional.empty();
	}

	@Override
	public void receiveAccept(int connectionID, SocketAddress from) {
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
	public void receiveReject(int connectionID, SocketAddress from) {
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
	public List<ChildSocket> getChildren() {
		return children;
	}

	@Override
	public UDPSocketWrapper getUDPWrapper() {
		return udpWrapper;
	}

	@Override
	public void close() {
		synchronized (children) {
			for (ChildSocket child : children) {
				child.disconnect();
			}
		}
		receivingThread.end();
		heartbeatThread.end();
		retransmissionThread.end();
	}

	@Override
	public void receiveConnect(int connectionID, SocketAddress from) {
		if (receiveTest.test(new PotentialSocketConnection(from))) {
			ChildSocket socket = childSocketFactory.apply(this, connectionID, from);
			synchronized (children) {
				children.add(socket);
			}
			try {
				udpWrapper.send(intToBytes(connectionID | SocketConstants.ACCEPT), from);
			} catch (IOException e) {
				System.err.println("IOException while accepting connection");
				e.printStackTrace();
			}
			receiveHandler.accept(socket);
		} else {
			try {
				udpWrapper.send(intToBytes(connectionID | SocketConstants.REJECT), from);
			} catch (IOException e) {
				System.err.println("IOException while rejecting connection");
				e.printStackTrace();
			}
		}
	}

}
