package com.phoenixkahlo.nodenet;

import com.phoenixkahlo.nodenet.serialization.FieldSerializer;
import com.phoenixkahlo.nodenet.serialization.Serializer;

/**
 * A payload to be transmitted virally representing that a connection has been severed.
 */
public class DisconnectionNotification {

	public static Serializer serializer(Serializer subSerializer) {
		return new FieldSerializer(DisconnectionNotification.class, subSerializer, DisconnectionNotification::new);
	}

	private NodeAddress node1;
	private NodeAddress node2;

	private DisconnectionNotification() {
	}

	public DisconnectionNotification(NodeAddress node1, NodeAddress node2) {
		this.node1 = node1;
		this.node2 = node2;
	}
	
	public NodeAddress get1() {
		return node1;
	}
	
	public NodeAddress get2() {
		return node2;
	}

}
