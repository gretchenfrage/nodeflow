package com.phoenixkahlo.nodenet.stream;

import static com.phoenixkahlo.nodenet.serialization.SerializationUtils.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class BasicChildStream implements ChildStream {

	private StreamFamily family;
	private int connectionID;
	private SocketAddress sendTo;

	// Synchronize usages of unconfirmed
	private List<UnconfirmedPayload> unconfirmed = new ArrayList<>();
	private AtomicInteger nextSendOrdinal = new AtomicInteger(0);

	private Queue<ReceivedMessage> receivedOrdered = new PriorityQueue<>(10,
			(received1, received2) -> received2.getOrdinal().getAsInt() - received1.getOrdinal().getAsInt());
	private Queue<ReceivedMessage> receivedUnordered = new LinkedList<>();
	private Object receivedLock = new Object();

	// Synchronize usages of partiallyReceived
	private List<MessageBuilder> partiallyReceived = new ArrayList<>();

	private volatile long lastHeartbeat;
	private long timeOfCreation = System.currentTimeMillis();

	private BiFunction<Integer, OptionalInt, MessageBuilder> messageBuilderFactory;
	private Runnable disconnectionHandler = () -> System.out.println(BasicChildStream.this + " disconnected");

	private volatile boolean disconnected = false;

	public BasicChildStream(StreamFamily family, int connectionID, SocketAddress sendTo,
			BiFunction<Integer, OptionalInt, MessageBuilder> messageBuilderFactory) {
		if ((connectionID & DatagramStreamConfig.TRANSMISSION_TYPE_RANGE) != 0)
			throw new IllegalArgumentException("connectionID has bits in transmission type range");
		this.family = family;
		this.connectionID = connectionID;
		this.sendTo = sendTo;
		this.messageBuilderFactory = messageBuilderFactory;
	}

	public BasicChildStream(StreamFamily family, int connectionID, SocketAddress sendTo) {
		if ((connectionID & DatagramStreamConfig.TRANSMISSION_TYPE_RANGE) != 0)
			throw new IllegalArgumentException("connectionID has bits in transmission type range");
		this.family = family;
		this.connectionID = connectionID;
		this.sendTo = sendTo;
		this.messageBuilderFactory = BasicMessageBuilder::new;
	}

	@Override
	public void send(byte[] data) throws DisconnectionException {
		sendMessage(data, OptionalInt.empty());
	}

	@Override
	public void sendOrdered(byte[] data) throws DisconnectionException {
		sendMessage(data, OptionalInt.of(nextSendOrdinal.getAndIncrement()));
	}

	private void sendMessage(byte[] message, OptionalInt ordinal) throws DisconnectionException {
		if (disconnected)
			throw new DisconnectionException();
		int messageID = ThreadLocalRandom.current().nextInt();
		byte[][] payloads = split(message, DatagramStreamConfig.MAX_PAYLOAD_SIZE);
		for (byte i = 0; i < payloads.length; i++) {
			sendPayload(payloads[i], messageID, ordinal, i, (byte) payloads.length);
		}
	}

	private void sendPayload(byte[] payload, int messageID, OptionalInt ordinal, byte partNumber, byte totalParts)
			throws DisconnectionException {
		int payloadID = ThreadLocalRandom.current().nextInt();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			if (ordinal.isPresent())
				baos.write(intToBytes(connectionID | DatagramStreamConfig.ORDERED_PAYLOAD));
			else
				baos.write(intToBytes(connectionID | DatagramStreamConfig.PAYLOAD));
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

		try {
			family.getUDPWrapper().send(baos.toByteArray(), sendTo);
		} catch (IOException e) {
			System.err.println("IOException on initial attempt of transmission");
			e.printStackTrace();
		}
	}

	@Override
	public byte[] receive() throws DisconnectionException {
		if (disconnected)
			throw new DisconnectionException();
		try {
			synchronized (receivedLock) {
				while (receivedOrdered.isEmpty() && receivedUnordered.isEmpty()) {
					receivedLock.wait();
				}
				if (receivedOrdered.size() > 0 && receivedUnordered.size() > 0)
					if (ThreadLocalRandom.current().nextBoolean())
						return receivedOrdered.remove().getMessage();
					else
						return receivedUnordered.remove().getMessage();
				else if (receivedOrdered.size() > 0)
					return receivedOrdered.remove().getMessage();
				else
					return receivedUnordered.remove().getMessage();
			}
		} catch (InterruptedException e) {
			if (disconnected)
				throw new DisconnectionException();
			else
				throw new RuntimeException("Interrupted while receiving, but not disconnected.");
		}
	}

	@Override
	public void disconnect() {
		disconnected = true;
		try {
			family.getUDPWrapper().send(intToBytes(connectionID | DatagramStreamConfig.DISCONNECT), sendTo);
		} catch (IOException e) {
			synchronized (System.err) {
				System.err.println("IOException while sending disconnect message");
				e.printStackTrace();
			}
		}
		synchronized (family.getChildren()) {
			family.getChildren().remove(this);
		}
		synchronized (receivedLock) {
			receivedLock.notifyAll();
		}
		disconnectionHandler.run();
	}

	@Override
	public void receiveDisconnect() {
		disconnected = true;
		synchronized (family.getChildren()) {
			family.getChildren().remove(this);
		}
		disconnectionHandler.run();
	}

	@Override
	public void receivePayload(ReceivedPayload payload) {
		try {
			family.getUDPWrapper().send(concatenate(intToBytes(connectionID | DatagramStreamConfig.CONFIRM),
					intToBytes(payload.getPayloadID())), sendTo);
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
				ReceivedMessage message = builder.toReceived();
				synchronized (receivedLock) {
					if (message.getOrdinal().isPresent())
						receivedOrdered.add(message);
					else
						receivedUnordered.add(message);
					receivedLock.notifyAll();
				}
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
			family.getUDPWrapper().send(intToBytes(connectionID | DatagramStreamConfig.HEARTBEAT), sendTo);
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
				if (time - payload.getLastSentTime() > DatagramStreamConfig.RETRANSMISSION_THRESHHOLD) {
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
	public void setDisconnectHandler(Runnable handler, boolean launchNewThread) {
		if (disconnected)
			if (launchNewThread)
				new Thread(handler).start();
			else
				handler.run();
		if (launchNewThread)
			this.disconnectionHandler = () -> new Thread(handler).start();
		else
			this.disconnectionHandler = handler;
	}

	@Override
	public long getTimeOfCreation() {
		return timeOfCreation;
	}

	@Override
	public String toString() {
		return "BasicChildStream id=" + connectionID + " sendTo=" + sendTo;
	}

	@Override
	public boolean isDisconnected() {
		return disconnected;
	}

	@Override
	public List<byte[]> getUnconfirmed() {
		return unconfirmed.stream().map(UnconfirmedPayload::getTransmission).collect(Collectors.toList());
	}

}
