package com.phoenixkahlo.pnet;

import java.util.function.Consumer;

/**
 * A reification of a user of the network, which is allowed to be the local
 * user. Objects may be sent to and received from the user, and the user may be
 * disconnected. Object transmission follows a FIFO ordering.
 */
public interface NetworkUser {

	/**
	 * Attempt to send the object to the user, with which the user may do
	 * anything it pleases, including nothing.
	 */
	void send(Object object);

	/**
	 * Return an object that has been received
	 */
	Object receive();

	/**
	 * Set an object to be invoked upon the receiving of any objects. As long as
	 * a receiver is set, the receive method will block forever.
	 */
	void setReceiver(Consumer<Object> receiver);

	/**
	 * Remove any existing receiver, reverting to the queue format accessed by
	 * receive.
	 */
	void unsetReceiver();

	/**
	 * Disconnect from the NetworkUser the two are directly connected.
	 */
	void disconnect();

}
