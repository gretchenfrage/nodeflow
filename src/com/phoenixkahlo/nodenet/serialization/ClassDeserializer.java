package com.phoenixkahlo.nodenet.serialization;

import java.io.IOException;
import java.io.InputStream;

import com.phoenixkahlo.nodenet.ProtocolViolationException;

public class ClassDeserializer implements Deserializer {

	private static StringDeserializer stringDeserializer = new StringDeserializer();
	
	@Override
	public Object deserialize(InputStream in) throws IOException, ProtocolViolationException {
		String name = (String) stringDeserializer.deserialize(in);
		try {
			return Class.forName(name);
		} catch (ClassNotFoundException e) {
			throw new ProtocolViolationException(e);
		}
	}

	@Override
	public Serializer toSerializer() {
		return new ClassSerializer();
	}

}
