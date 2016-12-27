package com.phoenixkahlo.nodenet.stream;

import java.util.List;

/**
 * A connection to another DatagramStream.
 */
public interface DatagramStream {

	/**
	 * Guarentee that data arrives at the other socket to be receive()d.
	 */
	void send(byte[] data) throws DisconnectionException;

	/**
	 * Send the data, and guarentee that it is receive()d in relative order to
	 * all other data send with sendOrdered().
	 */
	void sendOrdered(byte[] data) throws DisconnectionException;

	/**
	 * Return data send from the other side, blocking until there is data
	 * available.
	 */
	byte[] receive() throws DisconnectionException;

	/**
	 * Sever the connection to the other side, releasing all resources, and
	 * possibly preventing any subsequent transmissions.
	 */
	void disconnect();

	/**
	 * Set a runnable that will be invoked whenever a disconnection occurs from
	 * either end. If this stream is already disconnected, invoke the handler
	 * immediately.
	 */
	void setDisconnectHandler(Runnable handler, boolean launchNewThread);

	default void setDisconnectHandler(Runnable handler) {
		setDisconnectHandler(handler, true);
	}

	/**
	 * @return whether this stream is disconnected to the other side.
	 */
	boolean isDisconnected();

	/**
	 * @return all of the transmissions that have not been confirmed to have
	 *         been received on the other side.
	 */
	List<byte[]> getUnconfirmed();

}
