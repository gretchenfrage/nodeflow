package com.phoenixkahlo.pnet;

import java.net.SocketAddress;
import java.util.Optional;

/**
 * Information bean for a potential connection.
 */
public interface PotentialConnection {

	/**
	 * @return the address of the potential connection.
	 */
	SocketAddress getAddress();

	/**
	 * Will return the NetworkUser only after the connection has been accepted.
	 */
	Optional<NetworkNode> getConnector();

}
