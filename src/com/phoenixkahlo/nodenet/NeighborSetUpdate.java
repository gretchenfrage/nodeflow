package com.phoenixkahlo.nodenet;

import java.util.Set;

import com.phoenixkahlo.nodenet.serialization.FieldSerializer;
import com.phoenixkahlo.nodenet.serialization.Serializer;

<<<<<<< HEAD
/**
 * An update of the set of neighbors that a particular node has. Described in package description.
 */
=======
>>>>>>> eb56286c0399094b26770a91c1ceb3d22c73ee44
public class NeighborSetUpdate implements ViralPayload {

	public static Serializer serializer(Serializer subSerializer) {
		return new FieldSerializer(NeighborSetUpdate.class, subSerializer, NeighborSetUpdate::new);
	}
<<<<<<< HEAD

	private NodeAddress node;
	private Set<NodeAddress> neighbors;

	private NeighborSetUpdate() {
	}

=======
	
	private NodeAddress node;
	private Set<NodeAddress> neighbors;

	private NeighborSetUpdate() {}
	
>>>>>>> eb56286c0399094b26770a91c1ceb3d22c73ee44
	public NeighborSetUpdate(NodeAddress node, Set<NodeAddress> neighbors) {
		this.node = node;
		this.neighbors = neighbors;
	}
<<<<<<< HEAD

	public NodeAddress getNode() {
		return node;
	}

=======
	
	public NodeAddress getNode() {
		return node;
	}
	
>>>>>>> eb56286c0399094b26770a91c1ceb3d22c73ee44
	public Set<NodeAddress> getNeighbors() {
		return neighbors;
	}

}
