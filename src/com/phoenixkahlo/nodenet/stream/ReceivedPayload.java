package com.phoenixkahlo.nodenet.stream;

import java.util.OptionalInt;

import com.phoenixkahlo.util.UUID;

/**
 * A bean for all the data in a received payload. Ordinal is an OptionalInt, and
 * as such, this class works for both ordered and unordered messages. Payloads
 * with a greater partNumber have a greater natural ordering.
 */
public class ReceivedPayload {

	private UUID payloadID;
	private UUID messageID;
	private OptionalInt ordinal;
	private byte partNumber;
	private byte totalParts;
	private byte[] payload;

	public ReceivedPayload(UUID payloadID, UUID messageID, int ordinal, byte partNumber, byte totalParts,
			byte[] payload) {
		this.payloadID = payloadID;
		this.messageID = messageID;
		this.ordinal = OptionalInt.of(ordinal);
		this.partNumber = partNumber;
		this.totalParts = totalParts;
		this.payload = payload;
	}

	public ReceivedPayload(UUID payloadID, UUID messageID, byte partNumber, byte totalParts, byte[] payload) {
		this.payloadID = payloadID;
		this.messageID = messageID;
		this.ordinal = OptionalInt.empty();
		this.partNumber = partNumber;
		this.totalParts = totalParts;
		this.payload = payload;
	}

	public UUID getPayloadID() {
		return payloadID;
	}

	public UUID getMessageID() {
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

}
