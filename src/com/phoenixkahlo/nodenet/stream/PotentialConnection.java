package com.phoenixkahlo.nodenet.stream;

import java.net.InetSocketAddress;

/**
 * A bean for potential DatagramStream connections. Currently only contains the
 * SocketAddress.
 */
public class PotentialConnection {

	private InetSocketAddress address;
	
	public PotentialConnection(InetSocketAddress address) {
		this.address = address;
	}
	
	public InetSocketAddress getAddress() {
		return address;
	}
	
}
