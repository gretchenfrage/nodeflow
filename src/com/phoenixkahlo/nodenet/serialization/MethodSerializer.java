package com.phoenixkahlo.nodenet.serialization;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;

public class MethodSerializer implements Serializer {

	private static Serializer classSerializer = new ClassSerializer();
	private static Serializer stringSerializer = new StringSerializer();
	private static Serializer classArraySerializer = new ArraySerializer(Class.class, classSerializer);
	
	@Override
	public boolean canSerialize(Object object) {
		return object instanceof Method;
	}

	@Override
	public void serialize(Object object, OutputStream out) throws IOException {
		if (!canSerialize(object))
			throw new IllegalArgumentException(object + " isn't a method");
		
		Method method = (Method) object;
		classSerializer.serialize(method.getDeclaringClass(), out);
		stringSerializer.serialize(method.getName(), out);
		classArraySerializer.serialize(method.getParameterTypes(), out);
	}

	@Override
	public Deserializer toDeserializer() {
		return new MethodDeserializer();
	}

}
