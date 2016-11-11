package com.phoenixkahlo.pnet.socket;

import java.util.OptionalInt;

/**
 * A message that has been received. Messages with lower ordinals have a greater
 * natural ordering, messages with no ordinals are equal to everything.
 */
public class ReceivedMessage implements Comparable<ReceivedMessage> {

	private byte[] message;
	private OptionalInt ordinal;

	public ReceivedMessage(byte[] message) {
		this.message = message;
		this.ordinal = OptionalInt.empty();
	}

	public ReceivedMessage(byte[] message, int ordinal) {
		this.message = message;
		this.ordinal = OptionalInt.of(ordinal);
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