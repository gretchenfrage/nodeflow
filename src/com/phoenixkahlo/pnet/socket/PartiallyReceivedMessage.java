package com.phoenixkahlo.pnet.socket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.OptionalInt;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A mutable collection of ReceivedPayloads that form a partial ReceivedMessage.
 */
public class PartiallyReceivedMessage {

	private long messageID;
	private OptionalInt ordinal;
	private SortedSet<ReceivedPayload> received = new TreeSet<>();

	public PartiallyReceivedMessage(long messageID, int ordinal) {
		this.messageID = messageID;
		this.ordinal = OptionalInt.of(ordinal);
	}
	
	public PartiallyReceivedMessage(long messageID) {
		this.messageID = messageID;
		this.ordinal = OptionalInt.empty();
	}

	public long getMessageID() {
		return messageID;
	}

	public OptionalInt getOrdinal() {
		return ordinal;
	}

	public void addPayload(ReceivedPayload payload) {
		received.add(payload);
	}

	public boolean isComplete() {
		if (received.isEmpty())
			return false;
		boolean[] receivedArray = new boolean[received.first().getTotalParts()];
		for (ReceivedPayload payload : received) {
			receivedArray[payload.getPartNumber()] = true;
		}
		for (boolean receivedFlag : receivedArray) {
			if (!receivedFlag)
				return false;
		}
		return true;
	}

	public ReceivedMessage toReceived() {
		if (!isComplete())
			throw new IllegalStateException("Message not entirely received");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			for (ReceivedPayload payload : received) {
				baos.write(payload.getPayload());
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		if (ordinal.isPresent())
			return new ReceivedMessage(baos.toByteArray(), ordinal.getAsInt());
		else
			return new ReceivedMessage(baos.toByteArray());
	}

}
