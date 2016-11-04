package com.phoenixkahlo.pnet;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * A node in the network, which handles the UDP connection between all other
 * connected nodes.
 */
public interface NetworkConnection {

	/**
	 * Attempt to form a connection to another NetworkConnection. If the
	 * connection is successful, the connection will be returned. If not, an
	 * empty Optional will be returned. Failure may result from IOException,
	 * handshake failure, or rejectance.
	 */
	Optional<NetworkUser> outreach(String address, int port);

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
	 * @return all users connected to this network, directly or indirectly.
	 */
	List<NetworkUser> getUsers();

	/**
	 * Attempt to create a proxy for the given object that implements the given
	 * common interface. The proxy will delegate all method invocations to the
	 * original object. The proxy may be serializable, and even when send to
	 * other users of the network, will use the network to delegate method
	 * invocations to the original, blocking until a response is received. The
	 * originals to proxies may be saved for future remote invocations,
	 * preventing them from being garbage collected. A proxy original may be
	 * deleted with deleteProxy, after which all attempts by remote proxies to
	 * delegate to the original will yield exceptions.
	 */
	<E> E createProxy(E object, Class<E> intrface);

	/**
	 * Delete a proxy original as described in createProxy.
	 * 
	 * @return true if the proxy is deleted, false if the proxy has already been
	 *         deleted.
	 */
	boolean deleteProxy(Object proxy);

	/**
	 * Disconnect from all users.
	 */
	void disconnect();

}
