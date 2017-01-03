package com.phoenixkahlo.nodenet.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.function.BiFunction;

import com.phoenixkahlo.nodenet.ProtocolViolationException;

public class ThrowableDeserializer<E extends Throwable> implements Deserializer {

	private static Deserializer messageDeserializer = new NullableDeserializer(new StringDeserializer());
	private static Deserializer stackTraceDeserializer = new ArrayDeserializer(StackTraceElement.class,
			new FieldDeserializer(StackTraceElement.class, new NullableDeserializer(new StringDeserializer()),
					() -> new StackTraceElement("", "", "", 0)));

	private Class<E> type;
	private BiFunction<String, Throwable, E> factory;
	private Deserializer subDeserializer;

	public ThrowableDeserializer(Class<E> type, BiFunction<String, Throwable, E> factory,
			Deserializer subDeserializer) {
		this.type = type;
		this.factory = factory;
		this.subDeserializer = subDeserializer;
	}

	public ThrowableDeserializer(Class<E> type, BiFunction<String, Throwable, E> factory) {
		this.type = type;
		this.factory = factory;
	}

	@Override
	public Object deserialize(InputStream in) throws IOException, ProtocolViolationException {
		String message = (String) messageDeserializer.deserialize(in);
		Throwable cause = null;
		if (subDeserializer != null)
			cause = (Throwable) subDeserializer.deserialize(in);
		StackTraceElement[] stackTrace = (StackTraceElement[]) stackTraceDeserializer.deserialize(in);
		Throwable throwable = factory.apply(message, cause);

		try {
			Field stackTraceField = Throwable.class.getDeclaredField("stackTrace");
			stackTraceField.setAccessible(true);
			stackTraceField.set(throwable, stackTrace);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			throw new ProtocolViolationException(e);
		}

		return throwable;
	}

	@Override
	public Serializer toSerializer() {
		if (subDeserializer != null)
			return new ThrowableSerializer<>(type, factory, subDeserializer.toSerializer());
		else
			return new ThrowableSerializer<>(type, factory);
	}

}
