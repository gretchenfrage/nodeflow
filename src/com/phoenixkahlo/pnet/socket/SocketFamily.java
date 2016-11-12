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
	 * Set the test.serialization and handler for receiving new connections.
	 */
	void setReceiver(Predicate<PotentialSocketConnection> receiveTest, Consumer<PNetSocket> receiveHandler);

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

}
