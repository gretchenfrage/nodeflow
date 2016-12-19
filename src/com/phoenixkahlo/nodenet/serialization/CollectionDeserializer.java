package com.phoenixkahlo.pnet.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.function.Supplier;

import com.phoenixkahlo.pnet.ProtocolViolationException;

public class CollectionDeserializer<E extends Collection<?>> implements Deserializer {

	private Class<E> clazz;
	private Supplier<E> factory;
	private Deserializer subDeserializer;
	
	
	public CollectionDeserializer(Class<E> clazz, Supplier<E> factory, Deserializer subDeserializer) {
		this.clazz = clazz;
		this.factory = factory;
		this.subDeserializer = subDeserializer;
	}

	@Override
	public Object deserialize(InputStream in) throws IOException, ProtocolViolationException {
		int length = SerializationUtils.readInt(in);
		if (length < 0)
			throw new ProtocolViolationException("ArrayList cannot have negative size");
		@SuppressWarnings("unchecked")
		Collection<Object> collection = (Collection<Object>) factory.get();
		for (int i = 0; i < length; i++) {
			collection.add(subDeserializer.deserialize(in));
		}
		return collection;
	}

	@Override
	public Serializer toSerializer() {
		return new CollectionSerializer<>(clazz, factory, subDeserializer.toSerializer());
	}

}
