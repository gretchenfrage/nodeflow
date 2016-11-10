package com.phoenixkahlo.pnet.socket;

import java.net.InetAddress;

public class PotentialSocketConnectionBean implements PotentialSocketConnection {

	private InetAddress address;

	public PotentialSocketConnectionBean(InetAddress address) {
		this.address = address;
	}

	@Override
	public InetAddress getAddress() {
		return address;
	}

}
