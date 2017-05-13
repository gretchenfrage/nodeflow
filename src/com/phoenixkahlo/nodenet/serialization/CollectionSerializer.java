package com.phoenixkahlo.nodenet.serialization;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.function.Supplier;

public class CollectionSerializer<E extends Collection<?>> implements Serializer {

	private Class<E> clazz;
	private Supplier<E> factory;
	private Serializer subSerializer;
	private boolean flip;

	/**
	 * @param flip if you could iterate through the collection, and add each item to an empty instance of the same
	 *             collection, and get an equal collection, flip should be false. if you would get a backwards
	 *             collection, flip should be true.
	 */
	public CollectionSerializer(Class<E> clazz, Supplier<E> factory, Serializer subSerializer, boolean flip) {
		this.clazz = clazz;
		this.factory = factory;
		this.subSerializer = subSerializer;
		this.flip = flip;
	}

	public CollectionSerializer(Class<E> clazz, Supplier<E> factory, Serializer subSerializer) {
		this(clazz, factory, subSerializer, false);
	}

	@Override
	public boolean canSerialize(Object object) {
		return object != null && object.getClass() == clazz;
	}

	@Override
	public void serialize(Object object, OutputStream out) throws IOException {
		if (!canSerialize(object))
			throw new IllegalArgumentException("cannot serialize " + object);
		Collection<?> collection = (Collection<?>) object;
		SerializationUtils.writeInt(collection.size(), out);
		for (Object item : collection) {
			subSerializer.serialize(item, out);
		}
	}

	@Override
	public Deserializer toDeserializer() {
		return new CollectionDeserializer<>(clazz, factory, subSerializer.toDeserializer(), flip);
	}
	
}
