package com.phoenixkahlo.nodenet;

import java.util.function.Consumer;

/**
 * Representation of a node in the network.
 */
public interface Node {

	/**
	 * Receive an object from this node. This will block until an object is
	 * received. If a custom receiver is set, this method will block forever.
	 */
	Object receive() throws DisconnectionException;

	/**
	 * Set the handler for objects received from this node. This will change it
	 * from the default of making them available to the receive method, causing
	 * that method to block forever.
	 */
	void setReceiver(Consumer<Object> receiver);

	default void setReceiver(Consumer<Object> receiver, boolean launchNewThread) {
		if (launchNewThread)
			setReceiver(object -> new Thread(() -> receiver.accept(object)).start());
		else
			setReceiver(receiver);
	}
	
	/**
	 * Set the handler for objects received from this node to the default, such
	 * that they become available to the receive method.
	 */
	void resetReceiver();

	/**
	 * Send an object to this node.
	 */
	void send(Object object) throws DisconnectionException;

	/**
	 * Sever any DatagramStream with this node.
	 */
	boolean unlink();

	/**
	 * @return if the node is completely disconnected and all correspondences
	 *         will fail.
	 */
	boolean isDisconnected();

	NodeAddress getAddress();

}
