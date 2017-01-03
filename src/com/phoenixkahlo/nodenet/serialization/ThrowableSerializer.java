package com.phoenixkahlo.nodenet.serialization;

import java.io.IOException;
import java.io.OutputStream;
import java.util.function.BiFunction;

public class ThrowableSerializer<E extends Throwable> implements Serializer {

	private static Serializer messageSerializer = new NullableSerializer(new StringSerializer());
	private static Serializer stackTraceSerializer = new ArraySerializer(StackTraceElement.class, new FieldSerializer(
			StackTraceElement.class, new NullableSerializer(new StringSerializer()), () -> new StackTraceElement("", "", "", 0)));

	private Class<E> type;
	private BiFunction<String, Throwable, E> factory;
	private Serializer subSerializer;

	public ThrowableSerializer(Class<E> type, BiFunction<String, Throwable, E> factory,
			Serializer subSerializer) {
		this.type = type;
		this.factory = factory;
		this.subSerializer = subSerializer;
	}
	
	public ThrowableSerializer(Class<E> type, BiFunction<String, Throwable, E> factory) {
		this.type = type;
		this.factory = factory;
	}

	@Override
	public boolean canSerialize(Object object) {
		return object != null && object.getClass().equals(type);
	}

	@Override
	public void serialize(Object object, OutputStream out) throws IOException {
		if (!canSerialize(object))
			throw new IllegalArgumentException(object + " isn't instance of " + type);
		Throwable throwable = (Throwable) object;
		messageSerializer.serialize(throwable.getMessage(), out);
		if (subSerializer != null)
			subSerializer.serialize(throwable.getCause(), out);
		stackTraceSerializer.serialize(throwable.getStackTrace(), out);
	}

	@Override
	public Deserializer toDeserializer() {
		if (subSerializer != null)
			return new ThrowableDeserializer<>(type, factory, subSerializer.toDeserializer());
		else
			return new ThrowableDeserializer<>(type, factory);
	}

}
