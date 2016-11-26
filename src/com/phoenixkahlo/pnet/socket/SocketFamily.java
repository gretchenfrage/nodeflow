package com.phoenixkahlo.pnet.socket;

import java.net.SocketAddress;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A family of ChildSockets that are all bound to a particular local port. Owns
 * several threads that handle blocking based tasks for all children, preventing
 * extraneous thread creation. Will delegate to a settable predicate for whether
 * to accept new connections, and to a settable consumer for when new
 * connections are formed.
 */
public interface SocketFamily {

	/**
	 * Set the receive test and handler for new connections.
	 */
	default void setReceiver(Predicate<PotentialSocketConnection> receiveTest, Consumer<PNetSocket> receiveHandler) {
		setReceiveTest(receiveTest);
		setReceiveHandler(receiveHandler);
	}

	/**
	 * Set the test for receiving new connections. Should execute quickly, or
	 * will cause receiving thread to block, interrupting messages and heartbeat
	 * from all children..
	 */
	void setReceiveTest(Predicate<PotentialSocketConnection> receiveTest);

	/**
	 * Set the handler for receiving new connections. Should execute quickly, or
	 * will cause receiving thread to block, interrupting messages and heartbeat
	 * from all children..
	 */
	void setReceiveHandler(Consumer<PNetSocket> receiveHandler);

	/**
	 * Reject all new connections.
	 */
	default void disableReceiver() {
		setReceiver(c -> false, c -> {
		});
	}

	/**
	 * Attempt to connect to the address. Failure may result from timeout,
	 * IOException, rejectance, etc.
	 */
	Optional<PNetSocket> connect(SocketAddress address);

	/**
	 * Useage of children list should be synchronized.
	 */
	List<ChildSocket> getChildren();

	UDPSocketWrapper getUDPWrapper();

	/**
	 * Close all children and release all resources.
	 */
	void close();

	/**
	 * If the receiveTest allows the connection, broadcast a response and add to
	 * list of children.
	 */
	void receiveConnect(int connectionID, SocketAddress from);

	/**
	 * Realize the potential connection, add to the list of children, and return
	 * any threads waiting on connect.
	 */
	void receiveAccept(int connectionID, SocketAddress from);

	/**
	 * Return empty any threads waiting on connect.
	 */
	void receiveReject(int connectionID, SocketAddress from);

}
