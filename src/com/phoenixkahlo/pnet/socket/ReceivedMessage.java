package com.phoenixkahlo.pnet.socket;

import java.util.OptionalInt;

/**
 * A bean for all the data in a fully received message. Ordinal is an
 * OptionalInt, and as such, this works for both ordered and unordered messages.
 * Messages with lesser ordinals have a greater natural ordering. Any pair of
 * ReceivedMessages in which either is unordered have an equal natural ordering.
 */
public class ReceivedMessage implements Comparable<ReceivedMessage> {

	private byte[] message;
	private OptionalInt ordinal;

	public ReceivedMessage(byte[] message, OptionalInt ordinal) {
		this.message = message;
		this.ordinal = ordinal;
	}

	public byte[] getMessage() {
		return message;
	}

	@Override
	public int compareTo(ReceivedMessage other) {
		if (this.ordinal.isPresent() && other.ordinal.isPresent())
			return other.ordinal.getAsInt() - this.ordinal.getAsInt();
		else
			return 0;
	}

}