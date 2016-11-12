package com.phoenixkahlo.pnet.socket;

import java.util.OptionalInt;

/**
 * A group of ReceivedPayloads that will eventually compose a complete message.
 */
public interface MessageBuilder {

	long getMessageID();

	OptionalInt getOrdinal();

	void add(ReceivedPayload payload);
	
	boolean isComplete();
	
	ReceivedMessage toReceived();

}
