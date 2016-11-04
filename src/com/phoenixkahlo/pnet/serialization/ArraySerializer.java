package com.phoenixkahlo.pnet.serialization;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;

public class ArraySerializer implements Serializer {
	
	private Class<?> itemClass;
	private Serializer itemSerializer;
	
	public ArraySerializer(Class<?> itemClass, Serializer itemEncoder) {
		this.itemClass = itemClass;
		this.itemSerializer = itemEncoder;
	}
	
	public ArraySerializer(Class<?> itemClass) {
		this(itemClass, null);
	}
	
	@Override
	public boolean canSerialize(Object obj) {
		return (itemClass == int.class && obj instanceof int[]) ||
				(itemClass == long.class && obj instanceof long[]) ||
				(itemClass == double.class && obj instanceof double[]) ||
				(itemClass == float.class && obj instanceof float[]) ||
				(itemClass == short.class && obj instanceof short[]) ||
				(itemClass == char.class && obj instanceof char[]) ||
				(itemClass == boolean.class && obj instanceof boolean[]) ||
				(itemClass == byte.class && obj instanceof byte[]) ||
				(obj.getClass().isArray() && obj.getClass().getComponentType() == itemClass);
	}

	@Override
	public void serialize(Object obj, OutputStream out) throws IOException, IllegalArgumentException {
		if (!canSerialize(obj)) throw new IllegalArgumentException();
		int length = Array.getLength(obj);
		SerializationUtils.writeInt(length, out);
		for (int i = 0; i < length; i++) {
			SerializationUtils.serialize(Array.get(obj, i), itemClass, itemSerializer, out);
		}
	}

	@Override
	public Deserializer toDeserializer() {
		if (itemSerializer == null)
			return new ArrayDeserializer(itemClass);
		else
			return new ArrayDeserializer(itemClass, itemSerializer.toDeserializer());
	}

}
