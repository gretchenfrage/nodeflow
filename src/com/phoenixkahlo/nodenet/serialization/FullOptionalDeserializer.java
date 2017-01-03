package com.phoenixkahlo.nodenet.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import com.phoenixkahlo.nodenet.ProtocolViolationException;

public class FullOptionalDeserializer implements Deserializer {

	private Deserializer subDeserializer;
	
	public FullOptionalDeserializer(Deserializer subDeserializer) {
		this.subDeserializer = subDeserializer;
	}

	@Override
	public Object deserialize(InputStream in) throws IOException, ProtocolViolationException {
		return Optional.of(subDeserializer.deserialize(in));
	}

	@Override
	public Serializer toSerializer() {
		return new FullOptionalSerializer(subDeserializer.toSerializer());
	}

}
