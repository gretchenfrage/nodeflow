package com.phoenixkahlo.nodenet;

import com.phoenixkahlo.nodenet.serialization.FieldSerializer;
import com.phoenixkahlo.nodenet.serialization.Serializer;

<<<<<<< HEAD
/**
 * Described in package description.
 */
public class Handshake {

	// TODO: binary verification chunk

	public static Serializer serializer(Serializer subSerializer) {
		return new FieldSerializer(Handshake.class, subSerializer, Handshake::new);
	}

=======
public class Handshake {

	public static Serializer serializer(Serializer subSerializer) {
		return new FieldSerializer(Handshake.class, subSerializer, Handshake::new);
	}
	
>>>>>>> eb56286c0399094b26770a91c1ceb3d22c73ee44
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
