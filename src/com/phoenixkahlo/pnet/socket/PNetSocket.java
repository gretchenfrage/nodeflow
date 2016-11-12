package com.phoenixkahlo.pnet.socket;

import java.io.IOException;

/**
 * A connection to another PNetSocket.
 */
public interface PNetSocket {

	/**
	 * Guarentee that data arrives at the other socket to be receive()d.
	 */
	void send(byte[] data) throws IOException;

	/**
	 * Send the data, and guarentee that it is receive()d in relative order to
	 * all other data send with sendOrdered().
	 */
	void sendOrdered(byte[] data) throws IOException;

	/**
	 * Return data send from the other side, blocking until there is data
	 * available.
	 */
	byte[] receive();

	/**
	 * Sever the connection to the other side, releasing all resources, and
	 * possibly preventing any subsequent transmissions.
	 */
	void disconnect();

}
