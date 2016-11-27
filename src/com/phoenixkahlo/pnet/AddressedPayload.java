package com.phoenixkahlo.pnet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import com.phoenixkahlo.pnet.serialization.AutoSerializer;
import com.phoenixkahlo.pnet.serialization.FieldSerializer;
import com.phoenixkahlo.pnet.serialization.Serializer;

/**
 * <p>
 * An addessed payload that is designed to be relayed from one node to another
 * in a network.
 * </p>
 * <p>
 * An AddressedPayload has a destination node address, and a payload, which are
 * both immutable. It has an id, which is designed to be randomized between each
 * transmission. Finally, it has a set of node addresses which it has visited,
 * which is designed to be cumulative.
 * </p>
 * <p>
 * When a NetworkConnection receives an AddressedPayload, it should first check
 * if it's address is the payload's destination, in which case it should handle
 * the payload, and transmit a success message to where it was sent from. If the
 * payload is not yet at its destination, its job is to try to send the payload
 * to its destination, and then transmit a success or failure message to where
 * it was sent from.
 * </p>
 * <p>
 * When the NetworkConnection tries to send the payload to its destination, it
 * should add itself to the set of already visited nodes, and then, for each
 * adjacent node that is not in the set of already visited nodes, it should send
 * that node the addressed payload with the randomized ID, and wait for a
 * response. If the response is success, it should send a success message to
 * where the addressed payload was sent from, with the ID it was received with.
 * It should attempt to do this with all the nodes, in order of which one has
 * the shortest path to the destination without transversing any nodes it has
 * already transversed. If it attempts all applicable nodes without success, it
 * should send a failure signal to the original sender.
 * </p>
 * <p>
 * If the sender address is local, the NetworkConnection should not respond with
 * a success/failure message. If any transmission succeeds, it should simply
 * release relevant resources. If all transmissions fail, it should delegate to
 * a failure handler.
 * </p>
 * <p>
 * For convienence, the AddressedPayload provides access to its original ID - a
 * transient field that is made equal to the ID upon deserialization, such that
 * the ID can be randomized without forgetting the ID with which it was
 * received.
 * </p>
 */
public class AddressedPayload implements AutoSerializer {

	public static Serializer serializer(Serializer subSerializer) {
		return new FieldSerializer(AddressedPayload.class, subSerializer, AddressedPayload::new);
	}

	private Object payload;
	private NodeAddress destination;
	private Set<NodeAddress> visited;
	private int id;
	private transient OptionalInt originalID;

	private AddressedPayload() {
	}

	public AddressedPayload(Object payload, NodeAddress destination) {
		this.payload = payload;
		this.destination = destination;
		this.visited = new HashSet<>();
		randomizeID();
		originalID = OptionalInt.empty();
	}

	public void randomizeID() {
		id = ThreadLocalRandom.current().nextInt();
	}

	public Object getPayload() {
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

	public int getID() {
		return id;
	}

	/**
	 * Will throw exception if this object was not created by deserialization.
	 */
	public int getOriginalID() {
		return originalID.getAsInt();
	}

	@Override
	public void autoSerialize(OutputStream out) throws IOException {
	}

	@Override
	public void autoDeserialize(InputStream in) throws IOException, ProtocolViolationException {
		originalID = OptionalInt.of(id);
	}

}
