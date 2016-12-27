package com.phoenixkahlo.nodenet.serialization;

import java.io.IOException;
import java.io.InputStream;

import com.phoenixkahlo.nodenet.ProtocolViolationException;

/**
 * Deserializer that can only deserialize null. Symmetrical to NullSerializer.
 */
public class NullDeserializer implements Deserializer {

	@Override
	public Object deserialize(InputStream in) throws IOException, ProtocolViolationException {
		return null;
	}

	@Override
	public Serializer toSerializer() {
		return new NullSerializer();
	}

}
