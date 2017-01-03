package com.phoenixkahlo.nodenet.serialization;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;

public class FullOptionalSerializer implements Serializer {

	private Serializer subSerializer;
	
	public FullOptionalSerializer(Serializer subSerializer) {
		this.subSerializer = subSerializer;
	}

	@Override
	public boolean canSerialize(Object object) {
		return object != null && object instanceof Optional && ((Optional<?>) object).isPresent();
	}

	@Override
	public void serialize(Object object, OutputStream out) throws IOException {
		if (!canSerialize(object))
			throw new IllegalArgumentException(object + " isn't full optional");
		subSerializer.serialize(((Optional<?>) object).get(), out);
	}

	@Override
	public Deserializer toDeserializer() {
		return new FullOptionalDeserializer(subSerializer.toDeserializer());
	}
	
}
