package com.phoenixkahlo.nodenet.stream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import com.phoenixkahlo.nodenet.ProtocolViolationException;
import com.phoenixkahlo.nodenet.serialization.Deserializer;
import com.phoenixkahlo.nodenet.serialization.Serializer;

/**
 * A wrapper around a DatagramStream that uses a serialization/deserialization
 * service to send and receive objects instead of byte arrays.
 */
public class SerializerObjectStream implements ObjectStream {

	private Serializer serializer;
	private Deserializer deserializer;
	private DatagramStream socket;

	public SerializerObjectStream(DatagramStream socket, Serializer serializer) {
		this.socket = socket;
		this.serializer = serializer;
		rebuildDeserializer();
	}

	@Override
	public void rebuildDeserializer() {
		deserializer = serializer.toDeserializer();
	}

	@Override
	public void send(Object object) throws DisconnectionException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			serializer.serialize(object, baos);
		} catch (IOException e) {
			System.err.println("IOException while writing to BAOS");
			e.printStackTrace();
			throw new RuntimeException();
		}
		socket.send(baos.toByteArray());
	}

	@Override
	public void sendOrdered(Object object) throws DisconnectionException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			serializer.serialize(object, baos);
		} catch (IOException e) {
			System.err.println("IOException while writing to BAOS");
			e.printStackTrace();
			throw new RuntimeException();
		}
		socket.sendOrdered(baos.toByteArray());
	}

	@Override
	public Object receive() throws ProtocolViolationException, DisconnectionException {
		byte[] bin = socket.receive();
		InputStream in = new ByteArrayInputStream(bin);
		try {
			return deserializer.deserialize(in);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <E> E receive(Class<E> type) throws ProtocolViolationException, DisconnectionException {
		Object received = receive();
		if (type.isAssignableFrom(received.getClass()))
			return (E) received;
		else
			throw new ProtocolViolationException("Object is of wrong class: " + received);
	}

	@Override
	public void disconnect() {
		socket.disconnect();
	}

	@Override
	public void setDisconnectHandler(Runnable handler, boolean launchNewThread) {
		socket.setDisconnectHandler(handler, launchNewThread);
	}

	@Override
	public boolean isDisconnected() {
		return socket.isDisconnected();
	}

	@Override
	public List<Object> getUnconfirmed() {
		return socket.getUnconfirmed().stream().map(bin -> {
			try {
				return deserializer.deserialize(new ByteArrayInputStream(bin));
			} catch (IOException | ProtocolViolationException e) {
				return "[this object failed to deserialize because of " + e + "]";
			}
		}).collect(Collectors.toList());
	}

}
