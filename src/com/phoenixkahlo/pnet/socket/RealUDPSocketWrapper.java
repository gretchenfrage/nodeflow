package com.phoenixkahlo.pnet.socket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;

public class RealUDPSocketWrapper implements UDPSocketWrapper {

	private DatagramSocket socket;
	
	public RealUDPSocketWrapper(int port) throws SocketException {
		this.socket = new DatagramSocket(port);
	}
	
	public RealUDPSocketWrapper() throws SocketException {
		this.socket = new DatagramSocket();
	}
	
	@Override
	public void send(byte[] data, SocketAddress to) throws IOException {
		DatagramPacket packet = new DatagramPacket(data, data.length, to);
		socket.send(packet);
	}

	@Override
	public SocketAddress receive(byte[] buffer) throws IOException {
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		socket.receive(packet);
		return packet.getSocketAddress();
	}

}
