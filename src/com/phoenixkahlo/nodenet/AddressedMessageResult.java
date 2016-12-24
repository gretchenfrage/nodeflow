package com.phoenixkahlo.nodenet;

import com.phoenixkahlo.nodenet.serialization.FieldSerializer;
import com.phoenixkahlo.nodenet.serialization.Serializer;

<<<<<<< HEAD
/**
 * Described in package description.
 */
=======
>>>>>>> eb56286c0399094b26770a91c1ceb3d22c73ee44
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

}
