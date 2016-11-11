package com.phoenixkahlo.pnet.socket;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.util.Optional;

import com.phoenixkahlo.pnet.serialization.SerializationUtils;

/**
 * A thread that belongs to a SocketFamily which is responsible for reading UDP
 * datagrams and delegating their contents to the appropriate data structures.
 */
public class FamilyReceivingThread extends Thread {

	private SocketFamily family;
	private volatile boolean shouldContinue = true;

	public FamilyReceivingThread(SocketFamily family) {
		this.family = family;
	}

	@Override
	public void run() {
		byte[] buffer = new byte[SocketConstants.MAX_PAYLOAD_SIZE * 2];
		try {
			while (shouldContinue) {
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				family.getDatagramSocket().receive(packet);
				InputStream in = new ByteArrayInputStream(buffer);
				long header = SerializationUtils.readLong(in);
				long transmissionType = header & SocketConstants.TRANSMISSION_TYPE_MASK_RANGE;
				long connectionID = header & SocketConstants.CONNECTION_ID_RANGE;
				Optional<ChildSocket> socket;
				synchronized (family.getChildren()) {
					socket = family.getChildren().stream().filter(s -> s.getConnectionID() == connectionID).findAny();
				}
				if (transmissionType == SocketConstants.PAYLOAD_MASK) {
					if (socket.isPresent()) {
						long payloadID = SerializationUtils.readLong(in);
						long messageID = SerializationUtils.readLong(in);
						byte partNumber = (byte) in.read();
						byte totalParts = (byte) in.read();
						short payloadSize = SerializationUtils.readShort(in);
						byte[] payload = new byte[payloadSize];
						in.read(payload);
						ReceivedPayload received = new ReceivedPayload(payloadID, messageID, partNumber, totalParts, payload);
						socket.get().receivePayload(received);
					}
					
				} else if (transmissionType == SocketConstants.ORDERED_PAYLOAD_MASK) {
					if (socket.isPresent()) {
						long payloadID = SerializationUtils.readLong(in);
						long messageID = SerializationUtils.readLong(in);
						int ordinal = SerializationUtils.readInt(in);
						byte partNumber = (byte) in.read();
						byte totalParts = (byte) in.read();
						short payloadSize = SerializationUtils.readShort(in);
						byte[] payload = new byte[payloadSize];
						in.read(payload);
						ReceivedPayload received = new ReceivedPayload(payloadID, messageID, ordinal, partNumber, totalParts, payload);
						socket.get().receivePayload(received);
					}
				} else if (transmissionType == SocketConstants.START_CONNECTION_MASK) {
					family.receiveStartConnection(connectionID, packet.getAddress(), packet.getPort());
				} else if (transmissionType == SocketConstants.DISCONNECT_CONNECTION_MASK) {
					if (socket.isPresent()) {
						socket.get().receiveDisconnect();
					}
				} else if (transmissionType == SocketConstants.ACCEPT_CONNECTION_MASK) {
					family.receiveAcceptConnection(connectionID, packet.getAddress(), packet.getPort());
				} else if (transmissionType == SocketConstants.REJECT_CONNECTION_MASK) {
					family.receiveRejectConnection(connectionID);
				} else if (transmissionType == SocketConstants.CONFIRM_PAYLOAD_MASK) {
					if (socket.isPresent()) {
						long payloadID = SerializationUtils.readLong(in);
						socket.get().receiveConfirmation(payloadID);
					}
				} else if (transmissionType == SocketConstants.HEARTBEAT_MASK) {
					if (socket.isPresent())
						socket.get().receiveHeartbeat();
				} else {
					System.err.println("Invalid transmission type");
				}
			}
		} catch (IOException e) {
		}
	}

	public void kill() {
		shouldContinue = false;
		interrupt();
	}

}
