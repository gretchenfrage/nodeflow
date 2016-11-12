package com.phoenixkahlo.pnet.socket;

import java.net.SocketAddress;

/**
 * A bean for potential PNetSocket connections. Currently only contains the
 * SocketAddress.
 */
public class PotentialSocketConnection {

	private SocketAddress address;
	
	public PotentialSocketConnection(SocketAddress address) {
		this.address = address;
	}
	
	public SocketAddress getAddress() {
		return address;
	}
	
}
