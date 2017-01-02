package com.phoenixkahlo.nodenet.serialization;

import java.io.IOException;
import java.io.InputStream;

import com.phoenixkahlo.nodenet.ProtocolViolationException;

public class MethodDeserializer implements Deserializer {

	private static Deserializer classDeserializer = new ClassDeserializer();
	private static Deserializer stringDeserializer = new StringDeserializer();
	private static Deserializer classArrayDeserializer = new ArrayDeserializer(Class.class, classDeserializer);
	
	@Override
	public Object deserialize(InputStream in) throws IOException, ProtocolViolationException {
		Class<?> declaringClass = (Class<?>) classDeserializer.deserialize(in);
		String methodName = (String) stringDeserializer.deserialize(in);
		Class<?>[] paramTypes = (Class<?>[]) classArrayDeserializer.deserialize(in);
		
		try {
			return declaringClass.getMethod(methodName, paramTypes);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new ProtocolViolationException(e);
		}
	}

	@Override
	public Serializer toSerializer() {
		return new MethodSerializer();
	}

}
