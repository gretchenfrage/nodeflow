package com.phoenixkahlo.pnet;

import com.phoenixkahlo.pnet.serialization.FieldSerializer;
import com.phoenixkahlo.pnet.serialization.Serializer;

/**
 * Sent in response to an AddressedPayload as a success/failure message.
 */
public class AddressedPayloadResult {

	public static Serializer serializer(Serializer subSerializer) {
		return new FieldSerializer(AddressedPayloadResult.class, subSerializer, AddressedPayloadResult::new);
	}
	
	private int id;
	private boolean success;

	private AddressedPayloadResult() {
	}
	
	public AddressedPayloadResult(int id, boolean success) {
		this.id = id;
		this.success = success;
	}
	
	public int getID() {
		return id;
	}
	
	public boolean wasSuccessful() {
		return success;
	}
	
}
