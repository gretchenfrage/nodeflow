package com.phoenixkahlo.nodenet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import com.phoenixkahlo.nodenet.serialization.AutoSerializer;
import com.phoenixkahlo.nodenet.serialization.FieldSerializer;
import com.phoenixkahlo.nodenet.serialization.Serializer;
import com.phoenixkahlo.util.UUID;

/**
 * Described in package description.
 */
public class AddressedMessage implements AutoSerializer {

	public static Serializer serializer(Serializer subSerializer) {
		return new FieldSerializer(AddressedMessage.class, subSerializer, AddressedMessage::new);
	}

	private AddressedPayload payload;
	private NodeAddress sender;
	private NodeAddress destination;
	private Set<NodeAddress> visited;
	private UUID messageID;
	private UUID transmissionID;
	private transient UUID originalTransmissionID;

	private AddressedMessage() {
	}

	public AddressedMessage(AddressedPayload payload, NodeAddress sender, NodeAddress destination) {
		this.payload = payload;
		this.sender = sender;
		this.destination = destination;
		this.visited = new HashSet<>();
		this.messageID = new UUID();
		randomizeTransmissionID();
		this.originalTransmissionID = transmissionID;
	}

	public void randomizeTransmissionID() {
		transmissionID = new UUID();
	}

	public AddressedPayload getPayload() {
		return payload;
	}
	
	public NodeAddress getSender() {
		return sender;
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

	public UUID getMessageID() {
		return messageID;
	}
	
	public UUID getTransmissionID() {
		return transmissionID;
	}
	
	public UUID getOriginalTransmissionID() {
		return originalTransmissionID;
	}

	@Override
	public void autoSerialize(OutputStream out) throws IOException {
	}

	@Override
	public void autoDeserialize(InputStream in) throws IOException, ProtocolViolationException {
		originalTransmissionID = transmissionID;
	}
	
	@Override
	public String toString() {
		return "addressed(" + payload + ")";
	}

}
