package com.phoenixkahlo.pnet;

import java.util.function.Consumer;

/**
 * A reification NetworkConnection, local or remote.
 */
public interface NetworkNode {

	/**
	 * Attempt to send the object to the user, with which the user may do
	 * anything it pleases, including nothing.
	 */
	void send(Object object);

	/**
	 * Return an object that has been received. This method will block forever
	 * if a custom receiver is configured.
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
	 * Disconnect any direct connection between the two network users.
	 * 
	 * @return if a connection was broken, false if the users were never
	 *         directly connected.
	 */
	boolean disconnect();

}
