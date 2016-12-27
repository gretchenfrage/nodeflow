package com.phoenixkahlo.nodenet.serialization;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Serializer that can only serialize null. Symmetrical to NullDeserializer.
 */
public class NullSerializer implements Serializer {

	@Override
	public boolean canSerialize(Object object) {
		return object == null;
	}

	@Override
	public void serialize(Object object, OutputStream out) throws IOException {
		if (!canSerialize(object))
			throw new IllegalArgumentException(object + " isn't null");
	}

	@Override
	public Deserializer toDeserializer() {
		return new NullDeserializer();
	}

}
