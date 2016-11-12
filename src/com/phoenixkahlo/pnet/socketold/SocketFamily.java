package com.phoenixkahlo.pnet.socketold;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.phoenixkahlo.pnet.serialization.SerializationUtils;

/**
 * A family of PNetSockets all bound to the same local port.
 */@Deprecated
public class SocketFamily {

	private DatagramSocket datagramSocket;
	private List<ChildSocket> children = new ArrayList<>();
	private FamilyReceivingThread receivingThread;
	private FamilyHeartbeatThread sendingThread;
	private FamilyRetransmissionThread retransmissionThread;
	private List<UnconfirmedConnection> unconfirmedConnections = new ArrayList<>();
	private Predicate<PotentialSocketConnection> receiveTest;
	private Consumer<PNetSocket> receiveHandler;
	private boolean closed = false;

	/**
	 * Create a family bound to the particular local port.
	 */
	public SocketFamily(int port) throws SocketException {
		datagramSocket = new DatagramSocket(port);
		init();
	}

	/**
	 * Create a family bound to no particular port.
	 */
	public SocketFamily() throws SocketException {
		datagramSocket = new DatagramSocket();
		init();
	}

	private void init() {
		receivingThread = new FamilyReceivingThread(this);
		sendingThread = new FamilyHeartbeatThread(this);
		retransmissionThread = new FamilyRetransmissionThread(this);
		disableReceiver();
		receivingThread.start();
		sendingThread.start();
		retransmissionThread.start();
	}

	/**
	 * Set the test.serialization and handler for incoming connections.
	 */
	public void setReceiver(Predicate<PotentialSocketConnection> receiveTest, Consumer<PNetSocket> receiveHandler) {
		this.receiveTest = receiveTest;
		this.receiveHandler = receiveHandler;
	}

	/**
	 * Reject all incoming connections.
	 */
	public void disableReceiver() {
		setReceiver(p -> false, p -> {
		});
	}
	
	public void receiveStartConnection(long connectionID, InetAddress address, int port) {
		if (receiveTest.test(new PotentialSocketConnection(address, port))) {
			byte[] buffer = SerializationUtils.longToBytes(connectionID | SocketConstants.ACCEPT_CONNECTION_MASK);
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
			try {
				datagramSocket.send(packet);
			} catch (IOException e) {
			}
			
			ChildSocket socket = new ChildSocket(this, datagramSocket, connectionID, address, port);
			synchronized (children) {
				children.add(socket);
			}
			
			receiveHandler.accept(socket);
		}
	}

	/**
	 * Attempt to connect to the given address and port.
	 */
	public Optional<? extends PNetSocket> connect(InetAddress address, int port) throws IOException {
		if (closed)
			throw new IOException("Family closed");
		/*
		 * Send the message for creating a new connection, add an
		 * UnconfirmedConnection to unconfirmedConnections, and wait on
		 * unconfirmedConnections until the unconfirmedConnection has been
		 * removed. If there is now a ChildSocket with the generated ID in
		 * children, return it, otherwise, return empty.
		 */
		long connectionID = new Random().nextLong();

		byte[] buffer = SerializationUtils.longToBytes(connectionID | SocketConstants.START_CONNECTION_MASK);
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
		datagramSocket.send(packet);

		UnconfirmedConnection unconfirmed = new UnconfirmedConnection(connectionID);
		synchronized (unconfirmedConnections) {
			unconfirmedConnections.add(unconfirmed);
			while (unconfirmedConnections.contains(unconfirmed)) {
				try {
					unconfirmedConnections.wait();
				} catch (InterruptedException e) {
				}
			}
		}

		return children.stream().filter(child -> child.getConnectionID() == connectionID).findAny();
	}
	
	public void receiveAcceptConnection(long connectionID, InetAddress sendToAddress, int sendToPort) {
		synchronized (children) {
			children.add(new ChildSocket(this, datagramSocket, connectionID, sendToAddress, sendToPort));
		}
		synchronized (unconfirmedConnections) {
			unconfirmedConnections.removeIf(c -> c.getConnectionID() == connectionID);
			unconfirmedConnections.notifyAll();
		}
	}
	
	public void receiveRejectConnection(long connectionID) {
		synchronized (unconfirmedConnections) {
			unconfirmedConnections.removeIf(c -> c.getConnectionID() == connectionID);
			unconfirmedConnections.notifyAll();
		}
	}

	/**
	 * Disconnect all children, release all resources, kill all threads, stop
	 * everything.
	 */
	public void kill() {
		closed = true;
		synchronized (children) {
			for (ChildSocket child : children) {
				child.disconnect();
			}
		}
		datagramSocket.close();
		receivingThread.kill();
		sendingThread.kill();
	}

	/**
	 * Remove the child from the list of children.
	 */
	public void removeChild(ChildSocket child) {
		synchronized (children) {
			children.remove(child);
		}
	}

	public DatagramSocket getDatagramSocket() {
		return datagramSocket;
	}

	/**
	 * Children may be added to by the receiving thread and removed from by
	 * alien threads, as such, access to children should be synchronized.
	 */
	public List<ChildSocket> getChildren() {
		return children;
	}

}
