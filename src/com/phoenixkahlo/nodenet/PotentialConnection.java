package com.phoenixkahlo.pnet;

import java.net.SocketAddress;

/**
 * Information bean for a potential connection.
 */
public class PotentialConnection {

	private SocketAddress address;
	
	public PotentialConnection(SocketAddress address) {
		this.address = address;
	}
	
	/**
	 * @return the address of the potential connection.
	 */
	public SocketAddress getAddress() {
		return address;
	}

}
