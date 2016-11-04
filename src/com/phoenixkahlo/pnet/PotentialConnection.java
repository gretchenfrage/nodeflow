package com.phoenixkahlo.pnet;

import java.util.Optional;

/**
 * Information bean for a potential connection.
 */
public interface PotentialConnection {

	/**
	 * @return the IP address of the potential connection.
	 */
	String getAddress();

	/**
	 * Will return the NetworkUser only after the connection has been accepted.
	 */
	Optional<NetworkUser> getConnector();

}
