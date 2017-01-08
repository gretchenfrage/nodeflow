package com.phoenixkahlo.nodenet;

import com.phoenixkahlo.nodenet.serialization.FieldSerializer;
import com.phoenixkahlo.nodenet.serialization.Serializer;
import com.phoenixkahlo.util.UUID;

/**
 * Unique, random identifier for nodes.
 */
public class NodeAddress {

	public static Serializer serializer(Serializer subSerializer) {
		return new FieldSerializer(NodeAddress.class, subSerializer, NodeAddress::new);
	}

	private UUID id;

	public NodeAddress() {
	}

	public NodeAddress(UUID id) {
		this.id = id;
	}
	
	@Deprecated
	public NodeAddress(int n) {
		this.id = new UUID(n);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof NodeAddress)
			return id.equals(((NodeAddress) other).id);
		else
			return false;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
	
	@Override
	public String toString() {
		return id.toString();
	}

}
