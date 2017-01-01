package com.phoenixkahlo.nodenet.proxy;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.phoenixkahlo.nodenet.ProtocolViolationException;
import com.phoenixkahlo.nodenet.serialization.FieldSerializer;
import com.phoenixkahlo.nodenet.serialization.Serializer;

/**
 * A serializable representation of a method that can be turning into the actual
 * method.
 */
public class SerializableMethod {

	public Serializer serializer(Serializer subSerializer) {
		return new FieldSerializer(SerializableMethod.class, subSerializer, SerializableMethod::new);
	}

	private SerializableClass<?> declaringClass;
	private String name;
	private List<SerializableClass<?>> paramTypes;

	private SerializableMethod() {
	}

	@SuppressWarnings("unchecked")
	public SerializableMethod(Method method) {
		this.declaringClass = new SerializableClass<>(method.getDeclaringClass());
		this.name = method.getName();
		this.paramTypes = Arrays.stream(method.getParameterTypes()).map(SerializableClass::new)
				.collect(Collectors.toList());
	}
	
	public Method toMethod() throws ProtocolViolationException {
		Class<?>[] realParamTypes = new Class<?>[paramTypes.size()];
		for (int i = 0; i < paramTypes.size(); i++) {
			realParamTypes[i] = paramTypes.get(i).toClass();
		}
		try {
			return declaringClass.toClass().getMethod(name, realParamTypes);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new ProtocolViolationException(e);
		}
	}

}
