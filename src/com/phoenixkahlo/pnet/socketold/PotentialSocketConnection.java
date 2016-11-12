package com.phoenixkahlo.pnet.socketold;

import java.net.InetAddress;
@Deprecated
public class PotentialSocketConnection {

	private InetAddress address;
	private int port;

	public PotentialSocketConnection(InetAddress address, int port) {
		this.address = address;
		this.port = port;
	}

	public InetAddress getAddress() {
		return address;
	}
	
	public int getPort() {
		return port;
	}
	
}
