package com.phoenixkahlo.pnet.serialization;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;

public class ArraySerializer implements Serializer {

	private Class<?> itemClass;
	private Serializer itemSerializer; // Nullable for primitive arrays

	public ArraySerializer(Class<?> itemClass, Serializer itemSerialize) {
		this.itemClass = itemClass;
		this.itemSerializer = itemSerialize;
	}

	public ArraySerializer(Class<?> itemClass) {
		this(itemClass, null);
	}

	@Override
	public boolean canSerialize(Object obj) {
		return obj.getClass().isArray() && obj.getClass().getComponentType() == itemClass;
	}

	@Override
	public void serialize(Object obj, OutputStream out) throws IOException, IllegalArgumentException {
		if (!canSerialize(obj))
			throw new IllegalArgumentException("cannot serialize " + obj);
		int length = Array.getLength(obj);
		SerializationUtils.writeInt(length, out);
		for (int i = 0; i < length; i++)
			SerializationUtils.serialize(Array.get(obj, i), itemClass, itemSerializer, out);
	}

	@Override
	public Deserializer toDeserializer() {
		if (itemSerializer == null)
			return new ArrayDeserializer(itemClass);
		else
			return new ArrayDeserializer(itemClass, itemSerializer.toDeserializer());
	}

}
