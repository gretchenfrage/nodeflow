package com.phoenixkahlo.nodenet;

/**
 * Represents failure to follow a communication or serialization protocol.
 */
public class ProtocolViolationException extends Exception {

	private static final long serialVersionUID = -1345489555665409816L;

	public ProtocolViolationException() {
		super();
	}

	public ProtocolViolationException(String cause) {
		super(cause);
	}

	public ProtocolViolationException(Throwable cause) {
		super(cause);
	}

}
