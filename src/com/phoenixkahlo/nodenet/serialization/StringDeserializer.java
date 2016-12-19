package com.phoenixkahlo.nodenet.serialization;

import java.io.IOException;
import java.io.InputStream;

import com.phoenixkahlo.nodenet.ProtocolViolationException;

public class StringDeserializer implements Deserializer {

	@Override
	public Object deserialize(InputStream in) throws IOException, ProtocolViolationException {
		byte[] bin = SerializationUtils.deserializeByteArray(in);
		return SerializationUtils.bytesToString(bin);
	}

	@Override
	public Serializer toSerializer() {
		return new StringSerializer();
	}

}
