package com.phoenixkahlo.pnet.socket;

import java.io.IOException;
import java.net.SocketAddress;

/**
 * A wrapper for UDP operations, existing as an interface only to be mocked in
 * testing. All operations must be thread safe.
 */
public interface UDPSocketWrapper {

	/**
	 * Send the data to the destination.
	 */
	void send(byte[] data, SocketAddress to) throws IOException;

	/**
	 * Receive data into the buffer, blocking until done. Return the address
	 * from whence it came.
	 */
	SocketAddress receive(byte[] buffer) throws IOException;

}
