package com.phoenixkahlo.nodenet.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.phoenixkahlo.nodenet.ProtocolViolationException;

public class UnionDeserializer implements Deserializer {

	private Map<Integer, Deserializer> subDeserializers = new HashMap<>();
	// See UnionSerializer.toDeserializer
	private UnionSerializer toSerializer;

	public void add(int header, Deserializer deserializer) {
		subDeserializers.put(header, deserializer);
		toSerializer = null;
	}

	@Override
	public Object deserialize(InputStream in) throws IOException, ProtocolViolationException {
		Deserializer deserializer = subDeserializers.get(SerializationUtils.readInt(in));
		if (deserializer == null)
			throw new ProtocolViolationException("Invalid union header");
		return deserializer.deserialize(in);
	}

	@Override
	public Serializer toSerializer() {
		if (toSerializer == null) {
			toSerializer = new UnionSerializer();
			for (Map.Entry<Integer, Deserializer> entry : subDeserializers.entrySet()) {
				toSerializer.add(entry.getKey(), entry.getValue().toSerializer());
			}
		}
		return toSerializer;
	}

}
