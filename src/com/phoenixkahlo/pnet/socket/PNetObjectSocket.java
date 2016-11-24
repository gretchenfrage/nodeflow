package com.phoenixkahlo.pnet.socket;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.phoenixkahlo.pnet.ProtocolViolationException;
import com.phoenixkahlo.pnet.serialization.Deserializer;
import com.phoenixkahlo.pnet.serialization.Serializer;

/**
 * A wrapper around a PNetSocket that uses a serialization/deserialization
 * service to send and receive objects instead of byte arrays.
 */
public class PNetObjectSocket {

	private Serializer serializer;
	private Deserializer deserializer;
	private PNetSocket socket;

	public PNetObjectSocket(PNetSocket socket, Serializer serializer) {
		this.socket = socket;
		this.serializer = serializer;
		rebuildDeserializer();
	}

	public void rebuildDeserializer() {
		deserializer = serializer.toDeserializer();
	}

	public void send(Object object) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		serializer.serialize(object, baos);
		socket.send(baos.toByteArray());
	}

	public void sendOrdered(Object object) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		serializer.serialize(object, baos);
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
