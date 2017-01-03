package com.phoenixkahlo.nodenet.serialization;

import java.io.IOException;
import java.io.OutputStream;

public class ClassSerializer implements Serializer {

	private static StringSerializer stringSerializer = new StringSerializer();
	
	@Override
	public boolean canSerialize(Object object) {
		return object != null && object instanceof Class;
	}

	@Override
	public void serialize(Object object, OutputStream out) throws IOException {
		if (!canSerialize(object))
			throw new IllegalArgumentException(object + " is not a class");
		String name = ((Class<?>) object).getName();
		stringSerializer.serialize(name, out);
	}

	@Override
	public Deserializer toDeserializer() {
		return new ClassDeserializer();
	}

}
