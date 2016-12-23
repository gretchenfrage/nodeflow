package com.phoenixkahlo.nodenet;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import com.phoenixkahlo.nodenet.serialization.FieldSerializer;
import com.phoenixkahlo.nodenet.serialization.Serializer;

public class AddressedMessage {

	public static Serializer serializer(Serializer subSerializer) {
		return new FieldSerializer(AddressedMessage.class, subSerializer, AddressedMessage::new);
	}

	private AddressedPayload payload;
	private NodeAddress destination;
	private Set<NodeAddress> visited;
	private int transmissionID;

	private AddressedMessage() {
	}

	public AddressedMessage(AddressedPayload payload, NodeAddress destination) {
		this.payload = payload;
		this.destination = destination;
		this.visited = new HashSet<>();
		randomizeTransmissionID();
	}

	public void randomizeTransmissionID() {
		transmissionID = ThreadLocalRandom.current().nextInt();
	}

	public AddressedPayload getPayload() {
		return payload;
	}

	public NodeAddress getDestination() {
		return destination;
	}

	public Set<NodeAddress> getVisited() {
		return visited;
	}

	public void addVisited(NodeAddress node) {
		visited.add(node);
	}

	public int getTransmissionID() {
		return transmissionID;
	}

}
