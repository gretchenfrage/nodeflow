package com.phoenixkahlo.pnet;

/**
 * An object that implements ConnectionBindable will be bound to any
 * NetworkConnection deserializing it at the time of deserialization.
 */
public interface ConnectionBindable {

	void bindToConnection(NetworkConnection connection);

}
