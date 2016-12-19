package com.phoenixkahlo.nodenet.stream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.OptionalInt;
import java.util.SortedSet;
import java.util.TreeSet;

public class BasicMessageBuilder implements MessageBuilder {

	private int messageID;
	private OptionalInt ordinal;
	private SortedSet<ReceivedPayload> parts = new TreeSet<>(
			(payload1, payload2) -> payload1.getPartNumber() - payload2.getPartNumber());

	public BasicMessageBuilder(int messageID, OptionalInt ordinal) {
		this.messageID = messageID;
		this.ordinal = ordinal;
	}

	@Override
	public long getMessageID() {
		return messageID;
	}

	@Override
	public OptionalInt getOrdinal() {
		return ordinal;
	}

	@Override
	public void add(ReceivedPayload payload) {
		parts.add(payload);
	}

	@Override
	public boolean isComplete() {
		if (parts.isEmpty())
			return false;
		int lastPartNumber = -1;
		for (ReceivedPayload part : parts) {
			int currentPartNumber = part.getPartNumber();
			if (currentPartNumber - lastPartNumber > 1)
				return false;
			lastPartNumber = currentPartNumber;
		}
		return lastPartNumber == parts.first().getTotalParts() - 1;
	}

	@Override
	public ReceivedMessage toReceived() {
		if (!isComplete())
			throw new IllegalStateException("MessageBuilder not complete");

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			for (ReceivedPayload part : parts) {
				baos.write(part.getPayload());
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return new ReceivedMessage(baos.toByteArray(), parts.first().getOrdinal());
	}

}
