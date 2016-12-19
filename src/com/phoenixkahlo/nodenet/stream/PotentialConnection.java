package com.phoenixkahlo.pnet.socket;

import java.net.SocketAddress;

/**
 * A bean for potential DatagramStream connections. Currently only contains the
 * SocketAddress.
 */
public class PotentialConnection {

	private SocketAddress address;
	
	public PotentialConnection(SocketAddress address) {
		this.address = address;
	}
	
	public SocketAddress getAddress() {
		return address;
	}
	
}
