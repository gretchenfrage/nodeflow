package com.phoenixkahlo.nodenet;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import com.phoenixkahlo.nodenet.serialization.FieldSerializer;
import com.phoenixkahlo.nodenet.serialization.Serializer;

<<<<<<< HEAD
/**
 * Described in package description.
 */
=======
>>>>>>> eb56286c0399094b26770a91c1ceb3d22c73ee44
public class ViralMessage {

	public static Serializer serializer(Serializer subSerializer) {
		return new FieldSerializer(ViralMessage.class, subSerializer, ViralMessage::new);
	}

	private int virusID = ThreadLocalRandom.current().nextInt();
	private Set<NodeAddress> infectedNodes = new HashSet<>();
	private ViralPayload payload;

	private ViralMessage() {
	}

	public ViralMessage(ViralPayload payload) {
		this.virusID = ThreadLocalRandom.current().nextInt();
		this.infectedNodes = new HashSet<>();
		this.payload = payload;
	}

	public int getID() {
		return virusID;
	}

	public Set<NodeAddress> getInfected() {
		return infectedNodes;
	}

	public void addInfected(NodeAddress node) {
		infectedNodes.add(node);
	}

	public ViralPayload getPayload() {
		return payload;
	}

}
