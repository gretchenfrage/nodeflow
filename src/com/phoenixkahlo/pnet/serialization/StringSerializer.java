package com.phoenixkahlo.pnet.serialization;

import java.io.IOException;
import java.io.OutputStream;

public class StringSerializer implements Serializer {

	@Override
	public boolean canSerialize(Object object) {
		return object.getClass() == String.class;
	}

	@Override
	public void serialize(Object object, OutputStream out) throws IOException {
		if (!canSerialize(object)) throw new IllegalArgumentException();
		byte[] bin = SerializationUtils.stringToBytes((String) object);
		SerializationUtils.writeInt(bin.length, out);
		out.write(bin);
	}

	@Override
	public Deserializer toDeserializer() {
		return new StringDeserializer();
	}
	
}
