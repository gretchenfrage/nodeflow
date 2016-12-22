package com.phoenixkahlo.nodenet;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import com.phoenixkahlo.nodenet.serialization.FieldSerializer;
import com.phoenixkahlo.nodenet.serialization.Serializer;

public class ViralMessage {

	public static Serializer serializer(Serializer subSerializer) {
		return new FieldSerializer(ViralMessage.class, subSerializer, ViralMessage::new);
	}

	private int virusID = ThreadLocalRandom.current().nextInt();
	private Set<NodeAddress> infectedNodes = new HashSet<>();
	private Object payload;

	private ViralMessage() {
	}

	public ViralMessage(Object payload) {
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

	public Object getPayload() {
		return payload;
	}

}
