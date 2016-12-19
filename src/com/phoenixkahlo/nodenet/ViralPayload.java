package com.phoenixkahlo.pnet;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import com.phoenixkahlo.pnet.serialization.FieldSerializer;
import com.phoenixkahlo.pnet.serialization.Serializer;

/**
 * <p>
 * A viral payload that is designed to efficiently infect all nodes in a network
 * without each node only needing to be aware of its direct neighbors, allowing
 * viral payloads to be the means of notification for changes to a network.
 * </p>
 * <p>
 * Viral payloads have a unique payload ID, and a payload (both immutable) as
 * well as a set of nodes that have already been infected (mutable). When a
 * NetworkConnection receives a viral payload, it should* add the virus ID to
 * the set of virii it has handled, it should add itself to the virus' set of
 * nodes it has infected, it should send the modified virus to all adjacent
 * nodes not on the list of nodes that have already been infected, and it should
 * deal with the payload. *it should not do this if the virus ID is already on
 * the set of virii it has handled.
 * </p>
 */
public class ViralPayload {

	public static Serializer serializer(Serializer subSerializer) {
		return new FieldSerializer(ViralPayload.class, subSerializer, ViralPayload::new);
	}

	private int virusID = ThreadLocalRandom.current().nextInt();
	private Set<NodeAddress> infectedNodes = new HashSet<>();
	private Object payload;

	private ViralPayload() {
	}

	public ViralPayload(Object payload) {
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
