package com.phoenixkahlo.pnet.socket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Predicate;

import com.phoenixkahlo.pnet.ProtocolViolationException;
import com.phoenixkahlo.pnet.serialization.SerializationUtils;

public class UDPPNetSocket implements PNetSocket {

	private static final long HEARTBEAT_INTERVAL = 5_000;
	private static final int PAYLOAD_SIZE = 300;
	
	private static final long HEADER_MASK_RANGE = 0xF0000000;
	private static final long CONNECTION_ID_RANGE = ~HEADER_MASK_RANGE;

	/**
	 * A part of an unordered message. First 8 bytes are header, next 8 bytes
	 * are payload ID, next byte is message part number, next byte is total
	 * number of payloads in message, all following bytes are payload.
	 */
	private static final long PAYLOAD_MASK = 0x00000000;
	/**
	 * A part of an ordered message. First 8 bytes are header, next 8 bytes are
	 * payload ID, next 4 bytes are message ordinal, next byte is message part
	 * number, next byte is total number of payloads in message, all following
	 * bytes are payload.
	 */
	private static final long ORDERED_PAYLOAD_MASK = 0x10000000;
	/**
	 * A header-only message for reaching out to start a connection.
	 */
	private static final long START_CONNECTION_MASK = 0x20000000;
	/**
	 * A header-only messaage for ending a connection.
	 */
	private static final long END_CONNECTION_MASK = 0x30000000;
	/**
	 * A header-only message for accepting a proposed connection.
	 */
	private static final long ACCEPT_CONNECTION_MASK = 0x40000000;
	/**
	 * A header-only message for rejecting a proposed connection.
	 */
	private static final long REJECT_CONNECTION_MASK = 0x50000000;
	/**
	 * A confirmation that a payload has been received.
	 */
	private static final long CONFIRM_PAYLOAD_MASK = 0x60000000;
	/**
	 * A heartbeat that must be sent every HEARTBEAT_INTERVAL.
	 */
	private static final long HEARTBEAT_MASK = 0x70000000;

	private long connectionID;
	private InetAddress sendToAddress;
	private int sendToPort;
	private DatagramSocket socket = new DatagramSocket();

	/**
	 * Bind to a certain port and wait until another UDPPNetSocket reaches out
	 * to this address and port and is accepted by the test.
	 */
	public UDPPNetSocket(Predicate<PotentialSocketConnection> test, int port) throws IOException {
		boolean connected = false;
		while (!connected) {
			byte[] receiveBuffer = new byte[8];
			DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
			socket.receive(receivePacket);
			long receiveData = SerializationUtils.bytesToLong(receiveBuffer);
			long receiveHeader = receiveData & HEADER_MASK_RANGE;
			long receiveConnectionID = receiveData & CONNECTION_ID_RANGE;

			if (receiveHeader == START_CONNECTION_MASK) {
				if (test.test(new PotentialSocketConnectionBean(receivePacket.getAddress()))) {
					long sendData = receiveConnectionID | ACCEPT_CONNECTION_MASK;
					byte[] sendBuffer = SerializationUtils.longToBytes(sendData);
					DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length,
							receivePacket.getAddress(), receivePacket.getPort());
					socket.send(sendPacket);

					this.connectionID = receiveConnectionID;
					this.sendToAddress = receivePacket.getAddress();
					this.sendToPort = receivePacket.getPort();

					connected = true;
				} else {
					long sendData = receiveConnectionID | REJECT_CONNECTION_MASK;
					byte[] sendBuffer = SerializationUtils.longToBytes(sendData);
					DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length,
							receivePacket.getAddress(), receivePacket.getPort());
					socket.send(sendPacket);
				}
			}
		}
	}

	/**
	 * Attempt to connect to another UDPPNetSocket on a particular address and
	 * port.
	 */
	public UDPPNetSocket(InetAddress connectToAddress, int connectToPort)
			throws IOException, ProtocolViolationException {
		this.connectionID = new Random().nextLong() & CONNECTION_ID_RANGE;
		this.sendToAddress = connectToAddress;
		this.sendToPort = connectToPort;

		long sendData = connectionID | START_CONNECTION_MASK;
		byte[] sendBuffer = SerializationUtils.longToBytes(sendData);
		DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, sendToAddress, sendToPort);
		socket.send(sendPacket);

		byte[] receiveBuffer = new byte[8];
		DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
		socket.receive(receivePacket);
		long receiveData = SerializationUtils.bytesToLong(receiveBuffer);
		long receiveHeader = receiveData & HEADER_MASK_RANGE;
		long receiveConnectionID = receiveData & CONNECTION_ID_RANGE;
		if (receiveHeader == REJECT_CONNECTION_MASK)
			throw new IOException("Connection rejected");
		else if (receiveHeader != ACCEPT_CONNECTION_MASK || receiveConnectionID != connectionID)
			throw new ProtocolViolationException();

	}

	@Override
	public void send(byte[] message) throws IOException {
		
		
		/*
		long header = connectionID | PAYLOAD_MASK;
		byte[] sendBuffer = SerializationUtils.concatenate(SerializationUtils.longToBytes(header), data);
		DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, sendToAddress, sendToPort);
		socket.send(sendPacket);
		*/
	}

	@Override
	public byte[] receive() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub

	}

}
