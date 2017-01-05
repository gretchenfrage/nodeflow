package com.phoenixkahlo.nodenet;

import com.phoenixkahlo.nodenet.serialization.FieldSerializer;
import com.phoenixkahlo.nodenet.serialization.Serializer;

public class ClientTransmission implements AddressedPayload, ViralPayload {

	public static Serializer serializer(Serializer subSerializer) {
		return new FieldSerializer(ClientTransmission.class, subSerializer, ClientTransmission::new);
	}
	
	private Object object;

	private ClientTransmission() {
	}

	public ClientTransmission(Object payload) {
		this.object = payload;
	}

	public Object getObject() {
		return object;
	}

}
