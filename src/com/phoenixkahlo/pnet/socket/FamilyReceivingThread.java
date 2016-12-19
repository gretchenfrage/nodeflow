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
 * Helper thread for a StreamFamily. Waits on the UDPSocketWrapper to receive
 * datagrams, and delegates their data to the appropriates object.
 */
public class FamilyReceivingThread extends Thread implements EndableThread {

	private StreamFamily family;
	private volatile boolean shouldContinue = true;

	public FamilyReceivingThread(StreamFamily family) {
		this.family = family;
	}

	@Override
	public void run() {
		while (shouldContinue) {
			try {				
				byte[] buffer = new byte[DatagramStreamConfig.MAX_PAYLOAD_SIZE * 2];
				SocketAddress from = family.getUDPWrapper().receive(buffer);

				InputStream in = new ByteArrayInputStream(buffer);
				int header = readInt(in);
				int transmissionType = header & DatagramStreamConfig.TRANSMISSION_TYPE_RANGE;
				int connectionID = header & DatagramStreamConfig.CONNECTION_ID_RANGE;
								
				Optional<ChildStream> child;
				synchronized (family.getChildren()) {
					child = family.getChildren().stream().filter(c -> c.getConnectionID() == connectionID).findAny();
				}
				if (child.isPresent() && !child.get().getAlienAddress().equals(from)) {
					synchronized (System.err) {
						System.err.println("Transmission from " + from + " claiming to have connectionID "
								+ child.get().getConnectionID() + ", but that ID is associated with "
								+ child.get().getAlienAddress() + ". Transmissiong type: " + transmissionType + ".");
					}
					continue;
				}

				if (transmissionType == DatagramStreamConfig.CONNECT) {
					family.receiveConnect(connectionID, from);
				} else if (transmissionType == DatagramStreamConfig.ACCEPT) {
					family.receiveAccept(connectionID, from);
				} else if (transmissionType == DatagramStreamConfig.REJECT) {
					family.receiveReject(connectionID, from);
				} else { // All of these conditions require a connection to
							// already exist.
					if (child.isPresent()) {
						if (transmissionType == DatagramStreamConfig.PAYLOAD) {
							int payloadID = readInt(in);
							int messageID = readInt(in);
							byte partNumber = (byte) in.read();
							byte totalParts = (byte) in.read();
							short payloadSize = readShort(in);
							byte[] payload = new byte[payloadSize];
							in.read(payload);

							child.get().receivePayload(
									new ReceivedPayload(payloadID, messageID, partNumber, totalParts, payload));
						} else if (transmissionType == DatagramStreamConfig.ORDERED_PAYLOAD) {
							int payloadID = readInt(in);
							int messageID = readInt(in);
							int ordinal = readInt(in);
							byte partNumber = (byte) in.read();
							byte totalParts = (byte) in.read();
							short payloadSize = readShort(in);
							byte[] payload = new byte[payloadSize];
							in.read(payload);

							child.get().receivePayload(new ReceivedPayload(payloadID, messageID, ordinal, partNumber,
									totalParts, payload));
						} else if (transmissionType == DatagramStreamConfig.DISCONNECT) {
							child.get().receiveDisconnect();
						} else if (transmissionType == DatagramStreamConfig.CONFIRM) {
							int payloadID = readInt(in);
							child.get().receivePayloadConfirmation(payloadID);
						} else if (transmissionType == DatagramStreamConfig.HEARTBEAT) {
							child.get().receiveHeartbeat();
						} else {
							synchronized (System.err) {
								System.err.println("Invalid transmission type "
										+ DatagramStreamConfig.nameOf(transmissionType) + " received from " + from + ".");
							}
						}
					} else {
						synchronized (System.err) {
							System.err.println("Transmission received from " + from + " of type "
									+ DatagramStreamConfig.nameOf(transmissionType) + " and connectionID " + connectionID
									+ ", but no associated ChildStream exists.");
						}
					}
				}
			} catch (IOException e) {
				synchronized (System.err) {
					System.err.println("IOException in FamilyReceivingThread:");
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void end() {
		System.out.println("ENDING FAMILYRECEIVINGTHREAD");
		shouldContinue = false;
		interrupt();
	}

}
