package com.phoenixkahlo.pnet.serialization;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class HashMapSerializer implements Serializer {

	private Serializer keySerializer;
	private Serializer valueSerializer;
	
	public HashMapSerializer(Serializer keySerializer, Serializer valueSerializer) {
		this.keySerializer = keySerializer;
		this.valueSerializer = valueSerializer;
	}
	
	public HashMapSerializer(Serializer subSerializer) {
		this(subSerializer, subSerializer);
	}

	@Override
	public boolean canSerialize(Object object) {
		return object.getClass() == HashMap.class;
	}

	@Override
	public void serialize(Object object, OutputStream out) throws IOException {
		if (!canSerialize(object)) throw new IllegalArgumentException();
		Map<?, ?> map = (Map<?, ?>) object;
		SerializationUtils.writeInt(map.size(), out);
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			keySerializer.serialize(entry.getKey(), out);
			valueSerializer.serialize(entry.getValue(), out);
		}
	}

	@Override
	public Deserializer toDeserializer() {
		return new HashMapDeserializer(keySerializer.toDeserializer(), valueSerializer.toDeserializer());
	}

}
