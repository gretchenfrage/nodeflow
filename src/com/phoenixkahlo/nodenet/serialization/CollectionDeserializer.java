package com.phoenixkahlo.nodenet.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.phoenixkahlo.nodenet.ProtocolViolationException;

public class CollectionDeserializer<E extends Collection<?>> implements Deserializer {

	private Class<E> clazz;
	private Supplier<E> factory;
	private Deserializer subDeserializer;
	private boolean flip;
	
	public CollectionDeserializer(Class<E> clazz, Supplier<E> factory, Deserializer subDeserializer, boolean flip) {
		this.clazz = clazz;
		this.factory = factory;
		this.subDeserializer = subDeserializer;
		this.flip = flip;
	}

	@Override
	public Object deserialize(InputStream in) throws IOException, ProtocolViolationException {
		int length = SerializationUtils.readInt(in);
		if (length < 0)
			throw new ProtocolViolationException("ArrayList cannot have negative size");
		if (!flip) {
			@SuppressWarnings("unchecked")
			Collection<Object> collection = (Collection<Object>) factory.get();
			for (int i = 0; i < length; i++) {
				collection.add(subDeserializer.deserialize(in));
			}
			return collection;
		} else {
			List<Object> accumulator = new ArrayList<>();
			for (int i = 0; i < length; i++) {
				accumulator.add(subDeserializer.deserialize(in));
			}
			@SuppressWarnings("unchecked")
			Collection<Object> collection = (Collection<Object>) factory.get();
			for (int i = accumulator.size(); i >= 0; i--) {
				collection.add(accumulator.get(i));
			}
			return collection;
		}
	}

	@Override
	public Serializer toSerializer() {
		return new CollectionSerializer<>(clazz, factory, subDeserializer.toSerializer(), flip);
	}

}
