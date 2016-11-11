package com.phoenixkahlo.pnet.socket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.phoenixkahlo.pnet.serialization.SerializationUtils;

public class ChildSocket implements PNetSocket {

	private SocketFamily family;
	private List<UnconfirmedPayload> unconfirmed = new ArrayList<>();
	private List<PartiallyReceivedMessage> partiallyReceived = new ArrayList<>();
	private BlockingQueue<ReceivedMessage> received = new LinkedBlockingQueue<>();
	private long lastHeartbeatTime;
	private DatagramSocket datagramSocket;
	private long connectionID;
	private InetAddress sendToAddress;
	private int sendToPort;
	private Object nextSendOrdinalLock = new Object();
	private volatile int nextSendOrdinal = 0;
	private boolean disconnected = false;

	public ChildSocket(SocketFamily family, DatagramSocket socket, long connectionID, InetAddress sendToAddress,
			int sendToPort) {
		this.family = family;
		this.datagramSocket = socket;
		this.connectionID = connectionID;
		this.sendToAddress = sendToAddress;
		this.sendToPort = sendToPort;
	}

	@Override
	public void send(byte[] data) throws IOException {
		if (disconnected)
			throw new IOException("Socket disconnected");
		sendMessage(data, OptionalInt.empty());
	}

	@Override
	public void sendOrdered(byte[] data) throws IOException {
		if (disconnected)
			throw new IOException("Socket disconnected");
		int ordinal;
		synchronized (nextSendOrdinalLock) {
			ordinal = nextSendOrdinal;
			nextSendOrdinal++;
		}
		sendMessage(data, OptionalInt.of(ordinal));
	}

	private void sendMessage(byte[] message, OptionalInt ordinal) throws IOException {
		long messageID = new Random().nextLong();
		byte[][] payloads = SerializationUtils.split(message, SocketConstants.MAX_PAYLOAD_SIZE);
		for (byte i = 0; i < payloads.length; i++) {
			sendPayload(payloads[i], i, (byte) payloads.length, messageID, ordinal);
		}
	}

	public void sendPayload(byte[] payload, byte partNumber, byte totalParts, long messageID, OptionalInt ordinal)
			throws IOException {
		long payloadID = new Random().nextLong();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			if (ordinal.isPresent())
				baos.write(SerializationUtils.longToBytes(SocketConstants.ORDERED_PAYLOAD_MASK | connectionID));
			else
				baos.write(SerializationUtils.longToBytes(SocketConstants.PAYLOAD_MASK | connectionID));
			baos.write(SerializationUtils.longToBytes(payloadID));
			baos.write(SerializationUtils.longToBytes(messageID));
			if (ordinal.isPresent())
				baos.write(SerializationUtils.intToBytes(ordinal.getAsInt()));
			baos.write(partNumber);
			baos.write(totalParts);
			baos.write(SerializationUtils.shortToBytes((short) payload.length));
			baos.write(payload);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		byte[] buffer = baos.toByteArray();
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, sendToAddress, sendToPort);
		datagramSocket.send(packet);

		synchronized (unconfirmed) {
			unconfirmed.add(new UnconfirmedPayload(payloadID, buffer));
		}
	}

	@Override
	public byte[] receive() {
		return received.remove().getMessage();
	}

	@Override
	public void disconnect() {
		disconnected = true;
		byte[] buffer = SerializationUtils.longToBytes(connectionID | SocketConstants.DISCONNECT_CONNECTION_MASK);
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, sendToAddress, sendToPort);
		try {
			datagramSocket.send(packet);
		} catch (IOException e) {
		}
		family.removeChild(this);
	}

	public void receiveDisconnect() {
		disconnected = true;
		family.removeChild(this);
	}

	public void receivePayload(ReceivedPayload payload) throws IOException {
		if (disconnected)
			throw new IOException("Socket disconnected");

		giveConfirmation(payload.getPayloadID());

		Optional<PartiallyReceivedMessage> addToOptional = partiallyReceived.stream()
				.filter(partial -> partial.getMessageID() == payload.getMessageID()).findAny();
		PartiallyReceivedMessage addTo;
		if (addToOptional.isPresent()) {
			addTo = addToOptional.get();
		} else {
			if (payload.getOrdinal().isPresent())
				addTo = new PartiallyReceivedMessage(payload.getMessageID());
			else
				addTo = new PartiallyReceivedMessage(payload.getMessageID(), payload.getOrdinal().getAsInt());
			partiallyReceived.add(addTo);
		}
		addTo.addPayload(payload);

		if (addTo.isComplete()) {
			received.add(addTo.toReceived());
			partiallyReceived.remove(addTo);
		}
	}

	private void giveConfirmation(long payloadID) throws IOException {
		byte[] buffer = SerializationUtils.concatenate(
				SerializationUtils.longToBytes(connectionID | SocketConstants.CONFIRM_PAYLOAD_MASK),
				SerializationUtils.longToBytes(payloadID));
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, sendToAddress, sendToPort);
		datagramSocket.send(packet);
	}

	public void receiveConfirmation(long payloadID) {
		unconfirmed.removeIf(unconfirmed -> unconfirmed.getPayloadID() == payloadID);
	}

	public void receiveHeartbeat() {
		lastHeartbeatTime = System.currentTimeMillis();
	}

	public void sendHeartbeat() throws IOException {
		byte[] buffer = SerializationUtils.longToBytes(connectionID | SocketConstants.HEARTBEAT_MASK);
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, sendToAddress, sendToPort);
		datagramSocket.send(packet);
	}

	/**
	 * Since unconfirmed may be added to by alien threads, removed from by
	 * receiver threads, and read from by broadcasting threads, its usage should
	 * be synchronized.
	 */
	public List<UnconfirmedPayload> getUnconfirmed() {
		return unconfirmed;
	}

	public long getLastHeartbeatTime() {
		return lastHeartbeatTime;
	}

	public long getConnectionID() {
		return connectionID;
	}

	public void retransmitUnconfirmed() throws IOException {
		synchronized (unconfirmed) {
			for (UnconfirmedPayload payload : unconfirmed) {
				if (System.currentTimeMillis() - payload.getSentTime() > SocketConstants.RETRANSMISSION_INTERVAL) {
					DatagramPacket packet = new DatagramPacket(payload.getTransmission(), payload.getTransmission().length, sendToAddress, sendToPort);
					family.getDatagramSocket().send(packet);
				}
			}
		}
	}

}
