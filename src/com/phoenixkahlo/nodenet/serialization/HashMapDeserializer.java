package com.phoenixkahlo.nodenet.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.phoenixkahlo.nodenet.ProtocolViolationException;

public class HashMapDeserializer implements Deserializer {

	private Deserializer keyDeserializer;
	private Deserializer valueDeserializer;
	
	public HashMapDeserializer(Deserializer keyDeserializer, Deserializer valueDeserializer) {
		this.keyDeserializer = keyDeserializer;
		this.valueDeserializer = valueDeserializer;
	}

	public HashMapDeserializer(Deserializer subDeserializer) {
		this(subDeserializer, subDeserializer);
	}
	
	@Override
	public Object deserialize(InputStream in) throws IOException, ProtocolViolationException {
		int length = SerializationUtils.readInt(in);
		Map<Object, Object> map = new HashMap<Object, Object>();
		for (int i = 0; i < length; i++) {
			Object key = keyDeserializer.deserialize(in);
			Object value = valueDeserializer.deserialize(in);
			map.put(key, value);
		}
		return map;
	}

	@Override
	public Serializer toSerializer() {
		return new HashMapSerializer(keyDeserializer.toSerializer(), valueDeserializer.toSerializer());
	}

}
