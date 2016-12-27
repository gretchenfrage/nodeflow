package com.phoenixkahlo.nodenet;

/**
 * Thrown when a network operation is attempted on a disconnected stream.
 */
public class DisconnectionException extends Exception {

	private static final long serialVersionUID = -5869331322521397490L;

	public DisconnectionException() {
	}

	public DisconnectionException(String cause) {
		super(cause);
	}

	public DisconnectionException(Throwable cause) {
		super(cause);
	}

}
