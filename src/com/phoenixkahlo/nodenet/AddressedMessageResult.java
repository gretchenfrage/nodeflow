package com.phoenixkahlo.nodenet;

import com.phoenixkahlo.nodenet.serialization.FieldSerializer;
import com.phoenixkahlo.nodenet.serialization.Serializer;

/**
 * Described in package description.
 */
public class AddressedMessageResult {

	public static Serializer serializer(Serializer subSerializer) {
		return new FieldSerializer(AddressedMessageResult.class, subSerializer, AddressedMessageResult::new);
	}

	private int transmissionID;
	private boolean success;

	private AddressedMessageResult() {
	}

	public AddressedMessageResult(int transmissionID, boolean success) {
		this.transmissionID = transmissionID;
		this.success = success;
	}

	public int getTransmissionID() {
		return transmissionID;
	}

	public boolean wasSuccessful() {
		return success;
	}

	@Override
	public String toString() {
		return transmissionID + " was " + (success ? "" : "not") + " successful";
	}
	
}
