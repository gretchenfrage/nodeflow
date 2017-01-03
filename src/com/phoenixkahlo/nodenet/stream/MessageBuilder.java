package com.phoenixkahlo.nodenet.stream;

import java.util.OptionalInt;

import com.phoenixkahlo.util.UUID;

/**
 * A group of ReceivedPayloads that will eventually compose a complete message.
 */
public interface MessageBuilder {

	UUID getMessageID();

	OptionalInt getOrdinal();

	/**
	 * Must ignore duplicate payloads.
	 */
	void add(ReceivedPayload payload);
	
	boolean isComplete();
	
	
	ReceivedMessage toReceived();

}
