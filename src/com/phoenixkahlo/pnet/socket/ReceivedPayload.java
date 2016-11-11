package com.phoenixkahlo.pnet.socket;

import java.util.OptionalInt;

/**
 * A payload that has been received. Payloads with greater partNumbers have a
 * greater natural ordering.
 */
public class ReceivedPayload implements Comparable<ReceivedPayload> {

	private long payloadID;
	private long messageID;
	private OptionalInt ordinal;
	private byte partNumber;
	private byte totalParts;
	private byte[] payload;

	public ReceivedPayload(long payloadID, long messageID, int ordinal, byte partNumber, byte totalParts,
			byte[] payload) {
		this.payloadID = payloadID;
		this.messageID = messageID;
		this.ordinal = OptionalInt.of(ordinal);
		this.partNumber = partNumber;
		this.totalParts = totalParts;
		this.payload = payload;
	}

	public ReceivedPayload(long payloadID, long messageID, byte partNumber, byte totalParts, byte[] payload) {
		this.payloadID = payloadID;
		this.messageID = messageID;
		this.ordinal = OptionalInt.empty();
		this.partNumber = partNumber;
		this.totalParts = totalParts;
		this.payload = payload;
	}

	public long getPayloadID() {
		return payloadID;
	}

	public long getMessageID() {
		return messageID;
	}

	public OptionalInt getOrdinal() {
		return ordinal;
	}

	public byte getPartNumber() {
		return partNumber;
	}

	public byte getTotalParts() {
		return totalParts;
	}

	public byte[] getPayload() {
		return payload;
	}

	@Override
	public int compareTo(ReceivedPayload other) {
		return this.partNumber - other.partNumber;
	}

}
