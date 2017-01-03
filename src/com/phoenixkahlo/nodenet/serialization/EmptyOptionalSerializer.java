package com.phoenixkahlo.nodenet.serialization;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;

public class EmptyOptionalSerializer implements Serializer {

	@Override
	public boolean canSerialize(Object object) {
		return object != null && object instanceof Optional && !((Optional<?>) object).isPresent();
	}

	@Override
	public void serialize(Object object, OutputStream out) throws IOException {
		if (!canSerialize(object))
			throw new IllegalArgumentException(object + " isn't empty optional");
	}

	@Override
	public Deserializer toDeserializer() {
		return new EmptyOptionalDeserializer();
	}

}
