package com.phoenixkahlo.nodenet.stream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.phoenixkahlo.nodenet.ProtocolViolationException;
import com.phoenixkahlo.nodenet.serialization.Deserializer;
import com.phoenixkahlo.nodenet.serialization.Serializer;

/**
 * A wrapper around a DatagramStream that uses a serialization/deserialization
 * service to send and receive objects instead of byte arrays.
 */
public class ObjectStream {

	private Serializer serializer;
	private Deserializer deserializer;
	private DatagramStream socket;

	public ObjectStream(DatagramStream socket, Serializer serializer) {
		this.socket = socket;
		this.serializer = serializer;
		rebuildDeserializer();
	}

	public void rebuildDeserializer() {
		deserializer = serializer.toDeserializer();
	}

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

	public Object receive() throws ProtocolViolationException {
		byte[] bin = socket.receive();
		InputStream in = new ByteArrayInputStream(bin);
		try {
			return deserializer.deserialize(in);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	void disconnect() {
		socket.disconnect();
	}

	void setDisconnectHandler(Runnable handler) {
		socket.setDisconnectHandler(handler);
	}

}
