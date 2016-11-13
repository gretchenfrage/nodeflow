package com.phoenixkahlo.pnet.socket;

import static com.phoenixkahlo.pnet.serialization.SerializationUtils.readInt;
import static com.phoenixkahlo.pnet.serialization.SerializationUtils.readShort;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketAddress;
import java.util.Optional;

import com.phoenixkahlo.util.EndableThread;

/**
 * Helper thread for a SocketFamily. Waits on the UDPSocketWrapper to receive
 * datagrams, and delegates their data to the appropriates object.
 */
public class FamilyReceivingThread extends Thread implements EndableThread {

	private SocketFamily family;
	private volatile boolean shouldContinue = true;

	public FamilyReceivingThread(SocketFamily family) {
		this.family = family;
	}

	@Override
	public void run() {
		while (shouldContinue) {
			try {
				byte[] buffer = new byte[SocketConstants.MAX_PAYLOAD_SIZE * 2];
				SocketAddress from = family.getUDPWrapper().receive(buffer);
	
				InputStream in = new ByteArrayInputStream(buffer);
				int header = readInt(in);
				int transmissionType = header & SocketConstants.TRANSMISSION_TYPE_RANGE;
				int connectionID = header & SocketConstants.CONNECTION_ID_RANGE;
	
				Optional<ChildSocket> child;
				synchronized (family.getChildren()) {
					child = family.getChildren().stream().filter(c -> c.getConnectionID() == connectionID).findAny();
				}
				if (child.isPresent() && !child.get().getAlienAddress().equals(from)) {
					System.err.println("Transmission from " + from + " claiming to have connectionID "
							+ child.get().getConnectionID() + ", but that ID is associated with "
							+ child.get().getAlienAddress() + ". Transmissiong type: " + transmissionType + ".");
					continue;
				}
	
				if (transmissionType == SocketConstants.CONNECT) {
					family.receiveConnect(connectionID, from);
				} else if (transmissionType == SocketConstants.ACCEPT) {
					family.receiveAccept(connectionID, from);
				} else if (transmissionType == SocketConstants.REJECT) {
					family.receiveReject(connectionID, from);
				} else { // All of these conditions require a connection to already exist.
					if (child.isPresent()) {
						if (transmissionType == SocketConstants.PAYLOAD) {
							int payloadID = readInt(in);
							int messageID = readInt(in);
							byte partNumber = (byte) in.read();
							byte totalParts = (byte) in.read();
							short payloadSize = readShort(in);
							byte[] payload = new byte[payloadSize];
							in.read(payload);
							
							child.get().receivePayload(new ReceivedPayload(payloadID, messageID, partNumber, totalParts, payload));
						} else if (transmissionType == SocketConstants.ORDERED_PAYLOAD) {
							int payloadID = readInt(in);
							int messageID = readInt(in);
							int ordinal = readInt(in);
							byte partNumber = (byte) in.read();
							byte totalParts = (byte) in.read();
							short payloadSize = readShort(in);
							byte[] payload = new byte[payloadSize];
							in.read(payload);
							
							child.get().receivePayload(new ReceivedPayload(payloadID, messageID, ordinal, partNumber, totalParts, payload));
						} else if (transmissionType == SocketConstants.DISCONNECT) {
							child.get().receiveDisconnect();
						} else if (transmissionType == SocketConstants.CONFIRM) {
							int payloadID = readInt(in);
							child.get().receivePayloadConfirmation(payloadID);
						} else if (transmissionType == SocketConstants.HEARTBEAT) {
							child.get().receiveHeartbeat();
						} else {
							System.err.println("Invalid transmission type " + transmissionType + " received from " + from + ".");
						}
					} else {
						System.err.println("Transmission received from " + from + " of type " + transmissionType
								+ " and connectionID " + connectionID + ", but no associated ChildSocket exists.");
					}
				}
			} catch (IOException e) {
				System.err.println("IOException in FamilyReceivingThread:");
				e.printStackTrace();
			}
		}
	}

	@Override
	public void end() {
		shouldContinue = false;
		interrupt();
	}

}
