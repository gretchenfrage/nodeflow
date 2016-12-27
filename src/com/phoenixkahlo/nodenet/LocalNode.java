package com.phoenixkahlo.nodenet;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.phoenixkahlo.nodenet.serialization.Serializer;

/**
 * A connection to a node network.
 */
public interface LocalNode {

	/**
	 * Add a serializer for object transmission.
	 */
	void addSerializer(Serializer serializer, int header);

	/**
	 * Attempt to form a connection with the given address.
	 */
	Optional<Node> connect(InetSocketAddress address);

	/**
	 * Set the predicate that will determine whether incoming nodes will be
	 * accepted.
	 */
	void setGreeter(Predicate<InetSocketAddress> test);

	default void acceptAllIncoming() {
		setGreeter(address -> true);
	}
	
	default void rejectAllIncoming() {
		setGreeter(address -> false);
	}
	
	/**
	 * Add a listener for new nodes connected to the network.
	 */
	void listenForJoin(Consumer<Node> listener);

	/**
	 * Add a listener for nodes disconnected from the network.
	 */
	void listenForLeave(Consumer<Node> listener);

	/**
	 * Opposite of listenForJoin.
	 */
	void removeJoinListener(Consumer<Node> listener);
	
	/**
	 * Opposite of listenForLeave.
	 */
	void removeLeaveListener(Consumer<Node> listener);
	
	/**
	 * Get all the nodes in the network.
	 */
	List<Node> getNodes();

	/**
	 * Get all the adjacent nodes in the network.
	 */
	List<Node> getAdjacent();

	/**
	 * Completely disconnect from the network.
	 */
	void disconnect();

}
