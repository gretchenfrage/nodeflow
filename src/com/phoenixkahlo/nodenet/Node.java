package com.phoenixkahlo.nodenet;

import java.util.function.Consumer;

<<<<<<< HEAD
/**
 * Representation of a node in the network.
 */
=======
>>>>>>> eb56286c0399094b26770a91c1ceb3d22c73ee44
public interface Node {

	/**
	 * Receive an object from this node. This will block until an object is
	 * received. If a custom receiver is set, this method will block forever.
	 */
	Object receive();

	/**
	 * Set the handler for objects received from this node. This will change it
	 * from the default of making them available to the receive method, causing
	 * that method to block forever.
	 */
	void setReceiver(Consumer<Object> receiver);

	/**
	 * Set the handler for objects received from this node to the default, such
	 * that they become available to the receive method.
	 */
	void resetReceiver();

	/**
	 * Send an object to this node.
	 */
	void send(Object object);

	/**
	 * Sever any DatagramStream with this node.
	 */
	boolean disconect();

}
