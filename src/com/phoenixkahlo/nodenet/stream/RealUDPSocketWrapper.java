package com.phoenixkahlo.nodenet.stream;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

/**
 * A UDPSocketWrapper that binds to a local port and makes invocations to the
 * UDP protocol. Contrast to a mocked UDPSocketWrapper used in testing.
 */
public class RealUDPSocketWrapper implements UDPSocketWrapper {

	private DatagramSocket socket;

	public RealUDPSocketWrapper(int port) throws SocketException {
		this.socket = new DatagramSocket(port);
	}

	public RealUDPSocketWrapper() throws SocketException {
		this.socket = new DatagramSocket();
	}

	@Override
	public void send(byte[] data, InetSocketAddress to) throws IOException {
		DatagramPacket packet = new DatagramPacket(data, data.length, to);
		socket.send(packet);
	}

	@Override
	public InetSocketAddress receive(byte[] buffer) throws IOException {
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		socket.receive(packet);
		return new InetSocketAddress(packet.getAddress(), packet.getPort());
	}
	
	@Override
	public String toString() {
		return "RealUDPSocketWrapper localAddress=" + socket.getLocalSocketAddress();
	}

}
