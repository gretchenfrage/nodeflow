package com.phoenixkahlo.nodenet.stream;

import static com.phoenixkahlo.nodenet.serialization.SerializationUtils.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
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

import com.phoenixkahlo.nodenet.DisconnectionException;
import com.phoenixkahlo.util.UUID;

public class BasicChildStream implements ChildStream {

	private StreamFamily family;
	private UUID connectionID;
	private InetSocketAddress sendTo;

	// Synchronize usages of unconfirmed, and notify unconfirmed upon changes
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

	private BiFunction<UUID, OptionalInt, MessageBuilder> messageBuilderFactory;
	private Runnable disconnectionHandler = () -> System.out.println(BasicChildStream.this + " disconnected");

	private volatile boolean disconnected = false;

	private PrintStream err;

	public BasicChildStream(StreamFamily family, UUID connectionID, InetSocketAddress sendTo,
			BiFunction<UUID, OptionalInt, MessageBuilder> messageBuilderFactory, PrintStream err) {
		this.family = family;
		this.connectionID = connectionID;
		this.sendTo = sendTo;
		this.messageBuilderFactory = messageBuilderFactory;
		this.err = err;
	}

	public BasicChildStream(StreamFamily family, UUID connectionID, InetSocketAddress sendTo, PrintStream err) {
		this.family = family;
		this.connectionID = connectionID;
		this.sendTo = sendTo;
		this.messageBuilderFactory = BasicMessageBuilder::new;
		this.err = err;
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
		UUID messageID = new UUID();
		byte[][] payloads = split(message, DatagramStreamConfig.MAX_PAYLOAD_SIZE);
		for (byte i = 0; i < payloads.length; i++) {
			sendPayload(payloads[i], messageID, ordinal, i, (byte) payloads.length);
		}
	}

	private void sendPayload(byte[] payload, UUID messageID, OptionalInt ordinal, byte partNumber, byte totalParts)
			throws DisconnectionException {
		try {
			synchronized (unconfirmed) {
				while (unconfirmed.size() > DatagramStreamConfig.MAX_UNCONFIRMED_PAYLOADS)
					unconfirmed.wait();
			}
		} catch (InterruptedException e) {
			err.println("Interrupted while waiting for unconfirmed payloads to pass below threshhold");
		}

		UUID payloadID = new UUID();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			if (ordinal.isPresent()) {
				baos.write(DatagramStreamConfig.ORDERED_PAYLOAD);
				connectionID.write(baos);
				// baos.write(intToBytes(connectionID |
				// DatagramStreamConfig.ORDERED_PAYLOAD));
			} else {
				baos.write(DatagramStreamConfig.PAYLOAD);
				connectionID.write(baos);
				// baos.write(intToBytes(connectionID |
				// DatagramStreamConfig.PAYLOAD));
			}
			payloadID.write(baos);
			messageID.write(baos);
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
			err.println("IOException on initial attempt of transmission");
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
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			baos.write(DatagramStreamConfig.DISCONNECT);
			connectionID.write(baos);
			family.getUDPWrapper().send(baos.toByteArray(), sendTo);
			// family.getUDPWrapper().send(intToBytes(connectionID |
			// DatagramStreamConfig.DISCONNECT), sendTo);
		} catch (IOException e) {
			synchronized (err) {
				err.println("IOException while sending disconnect message");
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
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			baos.write(DatagramStreamConfig.CONFIRM);
			connectionID.write(baos);
			;
			payload.getPayloadID().write(baos);
			family.getUDPWrapper().send(baos.toByteArray(), sendTo);
			// family.getUDPWrapper().send(concatenate(intToBytes(connectionID |
			// DatagramStreamConfig.CONFIRM),
			// intToBytes(payload.getPayloadID())), sendTo);
		} catch (IOException e) {
			err.println("IOException while confirming payload");
			e.printStackTrace();
		}

		synchronized (partiallyReceived) {
			MessageBuilder builder;

			Optional<MessageBuilder> existingBuilder = partiallyReceived.stream()
					.filter(b -> b.getMessageID().equals(payload.getMessageID())).findAny();
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
	public void receivePayloadConfirmation(UUID payloadID) {
		synchronized (unconfirmed) {
			unconfirmed.removeIf(u -> u.getPayloadID().equals(payloadID));
			unconfirmed.notifyAll();
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
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			baos.write(DatagramStreamConfig.HEARTBEAT);
			connectionID.write(baos);
			family.getUDPWrapper().send(baos.toByteArray(), sendTo);
			// family.getUDPWrapper().send(intToBytes(connectionID |
			// DatagramStreamConfig.HEARTBEAT), sendTo);
		} catch (IOException e) {
			err.println("IOException while sending heartbeat");
			e.printStackTrace();
		}
	}

	@Override
	public UUID getConnectionID() {
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
						err.println("IOException while retransmitting");
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
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
		return "-->" + sendTo;
	}

	@Override
	public boolean isDisconnected() {
		return disconnected;
	}

	@Override
	public List<byte[]> getUnconfirmed() {
		synchronized (unconfirmed) {
			return unconfirmed.stream().map(UnconfirmedPayload::getTransmission).collect(Collectors.toList());
		}
	}

}
