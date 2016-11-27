package com.phoenixkahlo.pnet;

import com.phoenixkahlo.pnet.serialization.FieldSerializer;
import com.phoenixkahlo.pnet.serialization.Serializer;

/**
 * A payload to be transmitted virally representing that a new connection has
 * been formed.
 */
public class ConnectionNotification {

	public static Serializer serializer(Serializer subSerializer) {
		return new FieldSerializer(ConnectionNotification.class, subSerializer, ConnectionNotification::new);
	}

	private NodeAddress node1;
	private NodeAddress node2;

	private ConnectionNotification() {
	}

	public ConnectionNotification(NodeAddress node1, NodeAddress node2) {
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
