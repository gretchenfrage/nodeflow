package com.phoenixkahlo.nodenet;

<<<<<<< HEAD
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
=======
>>>>>>> eb56286c0399094b26770a91c1ceb3d22c73ee44
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

<<<<<<< HEAD
import com.phoenixkahlo.nodenet.serialization.AutoSerializer;
import com.phoenixkahlo.nodenet.serialization.FieldSerializer;
import com.phoenixkahlo.nodenet.serialization.Serializer;

/**
 * Described in package description.
 */
public class AddressedMessage implements AutoSerializer {
=======
import com.phoenixkahlo.nodenet.serialization.FieldSerializer;
import com.phoenixkahlo.nodenet.serialization.Serializer;

public class AddressedMessage {
>>>>>>> eb56286c0399094b26770a91c1ceb3d22c73ee44

	public static Serializer serializer(Serializer subSerializer) {
		return new FieldSerializer(AddressedMessage.class, subSerializer, AddressedMessage::new);
	}

	private AddressedPayload payload;
	private NodeAddress destination;
	private Set<NodeAddress> visited;
	private int transmissionID;
<<<<<<< HEAD
	private transient int originalTransmissionID;
=======
>>>>>>> eb56286c0399094b26770a91c1ceb3d22c73ee44

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
<<<<<<< HEAD
	
	public int getOriginalTransmissionID() {
		return originalTransmissionID;
	}

	@Override
	public void autoSerialize(OutputStream out) throws IOException {
	}

	@Override
	public void autoDeserialize(InputStream in) throws IOException, ProtocolViolationException {
		originalTransmissionID = transmissionID;
	}
=======
>>>>>>> eb56286c0399094b26770a91c1ceb3d22c73ee44

}
