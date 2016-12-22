package com.phoenixkahlo.nodenet;

import com.phoenixkahlo.nodenet.serialization.FieldSerializer;
import com.phoenixkahlo.nodenet.serialization.Serializer;

public class Handshake {

	public static Serializer serializer(Serializer subSerializer) {
		return new FieldSerializer(Handshake.class, subSerializer, Handshake::new);
	}
	
	private NodeAddress senderAddress;

	private Handshake() {
	}

	public Handshake(NodeAddress senderAddress) {
		this.senderAddress = senderAddress;
	}

	public NodeAddress getSenderAddress() {
		return senderAddress;
	}

}
