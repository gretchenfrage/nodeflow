package com.phoenixkahlo.nodenet.stream;

import java.util.OptionalInt;

/**
 * A group of ReceivedPayloads that will eventually compose a complete message.
 */
public interface MessageBuilder {

	long getMessageID();

	OptionalInt getOrdinal();

	/**
	 * Must ignore duplicate payloads.
	 */
	void add(ReceivedPayload payload);
	
	boolean isComplete();
	
	
	ReceivedMessage toReceived();

}
