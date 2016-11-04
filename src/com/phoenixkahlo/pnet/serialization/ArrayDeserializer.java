package com.phoenixkahlo.pnet.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;

import com.phoenixkahlo.pnet.ProtocolViolationException;

public class ArrayDeserializer implements Deserializer {

	private Class<?> itemClass;
	private Deserializer itemDeserializer;
	
	public ArrayDeserializer(Class<?> itemClass, Deserializer itemDeserializer) {
		this.itemClass = itemClass;
		this.itemDeserializer = itemDeserializer;
	}
	
	public ArrayDeserializer(Class<?> itemClass) {
		this(itemClass, null);
	}
	
	@Override
	public Object deserialize(InputStream in) throws IOException, ProtocolViolationException {
		int length = SerializationUtils.readInt(in);
		Object arr = Array.newInstance(itemClass, length);
		for (int i = 0; i < length; i++) {
			Array.set(arr, i, SerializationUtils.deserialize(itemClass, itemDeserializer, in));
		}
		return arr;
	}
	
	@Override
	public Serializer toSerializer() {
		if (itemDeserializer == null)
			return new ArraySerializer(itemClass);
		else
			return new ArraySerializer(itemClass, itemDeserializer.toSerializer());
	}
	
}
