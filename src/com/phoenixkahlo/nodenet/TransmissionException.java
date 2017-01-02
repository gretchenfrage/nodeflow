package com.phoenixkahlo.nodenet;

/**
 * Thrown when a transmission fails.
 */
public class TransmissionException extends Exception {

	private static final long serialVersionUID = -4437983124563399122L;

	public TransmissionException() {
	}

	public TransmissionException(String arg0) {
		super(arg0);
	}

	public TransmissionException(Throwable arg0) {
		super(arg0);
	}

}
