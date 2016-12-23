package com.phoenixkahlo.nodenet;

import java.util.Set;

import com.phoenixkahlo.nodenet.serialization.FieldSerializer;
import com.phoenixkahlo.nodenet.serialization.Serializer;

public class NeighborSetUpdate implements ViralPayload {

	public static Serializer serializer(Serializer subSerializer) {
		return new FieldSerializer(NeighborSetUpdate.class, subSerializer, NeighborSetUpdate::new);
	}
	
	private NodeAddress node;
	private Set<NodeAddress> neighbors;

	private NeighborSetUpdate() {}
	
	public NeighborSetUpdate(NodeAddress node, Set<NodeAddress> neighbors) {
		this.node = node;
		this.neighbors = neighbors;
	}
	
	public NodeAddress getNode() {
		return node;
	}
	
	public Set<NodeAddress> getNeighbors() {
		return neighbors;
	}

}
