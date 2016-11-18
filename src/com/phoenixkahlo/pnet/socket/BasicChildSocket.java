package com.phoenixkahlo.pnet.socket;

import static com.phoenixkahlo.pnet.serialization.SerializationUtils.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

public class BasicChildSocket implements ChildSocket {

	private Random random = new Random();
	private SocketFamily family;
	private int connectionID;
	private SocketAddress sendTo;
	// Synchronize usages of unconfirmed
	private List<UnconfirmedPayload> unconfirmed = new ArrayList<>();
	private AtomicInteger nextSendOrdinal = new AtomicInteger(0);
	private BlockingQueue<ReceivedMessage> received = new PriorityBlockingQueue<>(10, (received1, received2) -> {
		if (received1.getOrdinal().isPresent() && received2.getOrdinal().isPresent())
			return received2.getOrdinal().getAsInt() - received1.getOrdinal().getAsInt();
		else
			return 0;
	});
	// Synchronize usages of partiallyReceived
	private List<MessageBuilder> partiallyReceived = new ArrayList<>();
	private BiFunction<Integer, OptionalInt, MessageBuilder> messageBuilderFactory;
	private volatile long lastHeartbeat;
	private Runnable disconnectionHandler = () -> System.out.println(BasicChildSocket.this + " disconnected");
	private long timeOfCreation = System.currentTimeMillis();

	public BasicChildSocket(SocketFamily family, int connectionID, SocketAddress sendTo,
			BiFunction<Integer, OptionalInt, MessageBuilder> messageBuilderFactory) {
		if ((connectionID & SocketConstants.TRANSMISSION_TYPE_RANGE) != 0)
			throw new IllegalArgumentException("connectionID has bits in transmission type range");
		this.family = family;
		this.connectionID = connectionID;
		this.sendTo = sendTo;
		this.messageBuilderFactory = messageBuilderFactory;
	}

	public BasicChildSocket(SocketFamily family, int connectionID, SocketAddress sendTo) {
		if ((connectionID & SocketConstants.TRANSMISSION_TYPE_RANGE) != 0)
			throw new IllegalArgumentException("connectionID has bits in transmission type range");
		this.family = family;
		this.connectionID = connectionID;
		this.sendTo = sendTo;
		this.messageBuilderFactory = BasicMessageBuilder::new;
	}

	@Override
	public void send(byte[] data) throws IOException {
		sendMessage(data, OptionalInt.empty());
	}

	@Override
	public void sendOrdered(byte[] data) throws IOException {
		sendMessage(data, OptionalInt.of(nextSendOrdinal.getAndIncrement()));
	}

	private void sendMessage(byte[] message, OptionalInt ordinal) throws IOException {
		int messageID = random.nextInt();
		byte[][] payloads = split(message, SocketConstants.MAX_PAYLOAD_SIZE);
		for (byte i = 0; i < payloads.length; i++) {
			sendPayload(payloads[i], messageID, ordinal, i, (byte) payloads.length);
		}
	}

	private void sendPayload(byte[] payload, int messageID, OptionalInt ordinal, byte partNumber, byte totalParts)
			throws IOException {
		int payloadID = random.nextInt();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			if (ordinal.isPresent())
				baos.write(intToBytes(connectionID | SocketConstants.ORDERED_PAYLOAD));
			else
				baos.write(intToBytes(connectionID | SocketConstants.PAYLOAD));
			baos.write(intToBytes(payloadID));
			baos.write(intToBytes(messageID));
			if (ordinal.isPresent())
				baos.write(intToBytes(ordinal.getAsInt()));
			baos.write(partNumber);
			baos.write(totalParts);
			baos.write(shortToBytes((short) payload.length));
			baos.write(payload);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		synchronized (unconfirmed) {
			unconfirmed.add(new UnconfirmedPayload(payloadID, baos.toByteArray()));
		}

		family.getUDPWrapper().send(baos.toByteArray(), sendTo);
	}

	@Override
	public byte[] receive() {
		try {
			return received.take().getMessage();
		} catch (InterruptedException e) {
			throw new RuntimeException("Interrupted while waiting to receive message", e);
		}
	}

	@Override
	public void disconnect() {
		try {
			family.getUDPWrapper().send(intToBytes(connectionID | SocketConstants.DISCONNECT), sendTo);
		} catch (IOException e) {
			synchronized (System.err) {
				System.err.println("IOException while sending disconnect message");
				e.printStackTrace();
			}
		}
		synchronized (family.getChildren()) {
			family.getChildren().remove(this);
		}
		disconnectionHandler.run();
	}

	@Override
	public void receiveDisconnect() {
		synchronized (family.getChildren()) {
			family.getChildren().remove(this);
		}
		disconnectionHandler.run();
	}

	@Override
	public void receivePayload(ReceivedPayload payload) {
		try {
			family.getUDPWrapper().send(
					concatenate(intToBytes(connectionID | SocketConstants.CONFIRM), intToBytes(payload.getPayloadID())),
					sendTo);
		} catch (IOException e) {
			System.err.println("IOException while confirming payload");
			e.printStackTrace();
		}

		synchronized (partiallyReceived) {
			MessageBuilder builder;

			Optional<MessageBuilder> existingBuilder = partiallyReceived.stream()
					.filter(b -> b.getMessageID() == payload.getMessageID()).findAny();
			if (existingBuilder.isPresent()) {
				builder = existingBuilder.get();
			} else {
				builder = messageBuilderFactory.apply(payload.getMessageID(), payload.getOrdinal());
				partiallyReceived.add(builder);
			}

			builder.add(payload);
			if (builder.isComplete()) {
				partiallyReceived.remove(builder);
				received.add(builder.toReceived());
			}
		}
	}

	@Override
	public void receivePayloadConfirmation(int payloadID) {
		synchronized (unconfirmed) {
			unconfirmed.removeIf(u -> u.getPayloadID() == payloadID);
		}
	}

	@Override
	public long getLastHeartbeat() {
		return lastHeartbeat;
	}

	@Override
	public void receiveHeartbeat() {
		lastHeartbeat = System.currentTimeMillis();
	}

	@Override
	public void sendHeartbeat() {
		try {
			family.getUDPWrapper().send(intToBytes(connectionID | SocketConstants.HEARTBEAT), sendTo);
		} catch (IOException e) {
			System.err.println("IOException while sending heartbeat");
			e.printStackTrace();
		}
	}

	@Override
	public int getConnectionID() {
		return connectionID;
	}

	@Override
	public void retransmitUnconfirmed() {
		synchronized (unconfirmed) {
			long time = System.currentTimeMillis();
			for (UnconfirmedPayload payload : unconfirmed) {
				if (time - payload.getLastSentTime() > SocketConstants.RETRANSMISSION_THRESHHOLD) {
					try {
						family.getUDPWrapper().send(payload.getTransmission(), sendTo);
						payload.setLastSendTime(time);
					} catch (IOException e) {
						System.err.println("IOException while retransmitting");
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public SocketAddress getAlienAddress() {
		return sendTo;
	}

	@Override
	public void setDisconnectHandler(Runnable handler) {
		this.disconnectionHandler = handler;
	}

	@Override
	public long getTimeOfCreation() {
		return timeOfCreation;
	}

	@Override
	public String toString() {
		return "BasicChildSocket id=" + connectionID + " sendTo=" + sendTo;
	}

}
