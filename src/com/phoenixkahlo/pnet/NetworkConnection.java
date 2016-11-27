package com.phoenixkahlo.pnet;

import java.net.SocketAddress;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.phoenixkahlo.pnet.serialization.Serializer;

/**
 * A local node in a network.
 */
public interface NetworkConnection {

	/**
	 * Add the serializer with the header to the serialization/deserialization
	 * protocol.
	 */
	void addSerializer(Serializer serializer, int header);

	/**
	 * Attempt to form a connection to another NetworkConnection. If the
	 * connection is successful, the connection will be returned. If not, an
	 * empty Optional will be returned. Failure may result from IOException,
	 * handshake failure, or rejectance.
	 */
	Optional<NetworkNode> connect(SocketAddress address);

	/**
	 * Set the test for whether incoming connections will be accepted.
	 */
	void setIncomingTest(Predicate<PotentialConnection> test);

	/**
	 * Allow all incoming connections.
	 */
	default void enableIncoming() {
		setIncomingTest(potential -> true);
	}

	/**
	 * Disallow all incoming connections.
	 */
	default void disableIncoming() {
		setIncomingTest(potential -> false);
	}

	/**
	 * Provide a listener that will be invoked whenever an incoming connection
	 * is accepted. Will not notify when a connection is created from the local
	 * end.
	 */
	void listenForConnection(Consumer<NetworkNode> listener);

	/**
	 * @return all users connected to this network, directly or indirectly.
	 */
	List<NetworkNode> getNodes();

	/**
	 * @return all users directly connected to this local node.
	 */
	List<NetworkNode> getAdjacentNodes();

	/**
	 * Disconnect from all users and release all resources.
	 */
	void disconnect();

}
