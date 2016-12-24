package com.phoenixkahlo.nodenet;

import com.phoenixkahlo.nodenet.serialization.FieldSerializer;
import com.phoenixkahlo.nodenet.serialization.Serializer;
import com.phoenixkahlo.util.PerfectHashable;

<<<<<<< HEAD
/**
 * Unique, random identifier for nodes.
 */
=======
>>>>>>> eb56286c0399094b26770a91c1ceb3d22c73ee44
public class NodeAddress implements PerfectHashable {

	public static Serializer serializer(Serializer subSerializer) {
		return new FieldSerializer(NodeAddress.class, subSerializer, NodeAddress::new);
	}

	private int id;

	private NodeAddress() {
	}

	public NodeAddress(int id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof NodeAddress)
			return id == ((NodeAddress) other).id;
		else
			return false;
	}

	@Override
	public int hashCode() {
		return id;
	}

}
