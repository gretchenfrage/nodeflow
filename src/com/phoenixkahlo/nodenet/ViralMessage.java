package com.phoenixkahlo.nodenet;

import java.util.HashSet;
import java.util.Set;

import com.phoenixkahlo.nodenet.serialization.FieldSerializer;
import com.phoenixkahlo.nodenet.serialization.Serializer;
import com.phoenixkahlo.util.UUID;

/**
 * Described in package description.
 */
public class ViralMessage {

	public static Serializer serializer(Serializer subSerializer) {
		return new FieldSerializer(ViralMessage.class, subSerializer, ViralMessage::new);
	}

	private UUID virusID = new UUID();
	private Set<NodeAddress> infectedNodes = new HashSet<>();
	private ViralPayload payload;

	private ViralMessage() {
	}

	public ViralMessage(ViralPayload payload) {
		this.virusID = new UUID();
		this.infectedNodes = new HashSet<>();
		this.payload = payload;
	}

	public UUID getID() {
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
	
	@Override
	public String toString() {
		return "viral(" + payload + ")";
	}

}
