package com.phoenixkahlo.pnet;

import com.phoenixkahlo.pnet.serialization.FieldSerializer;
import com.phoenixkahlo.pnet.serialization.Serializer;
import com.phoenixkahlo.util.PerfectHashable;

/**
 * Represents an address for a node in the network.
 */
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
