package com.phoenixkahlo.nodenet;

public class ClientTransmission implements AddressedPayload {

	private Object object;

	public ClientTransmission(Object payload) {
		this.object = payload;
	}

	public Object getObject() {
		return object;
	}

}
