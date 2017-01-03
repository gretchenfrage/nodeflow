package com.phoenixkahlo.nodenet.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import com.phoenixkahlo.nodenet.ProtocolViolationException;

public class EmptyOptionalDeserializer implements Deserializer {

	@Override
	public Object deserialize(InputStream in) throws IOException, ProtocolViolationException {
		return Optional.empty();
	}

	@Override
	public Serializer toSerializer() {
		return new EmptyOptionalSerializer();
	}

}
