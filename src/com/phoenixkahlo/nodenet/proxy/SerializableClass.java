package com.phoenixkahlo.nodenet.proxy;

import com.phoenixkahlo.nodenet.ProtocolViolationException;
import com.phoenixkahlo.nodenet.serialization.FieldSerializer;
import com.phoenixkahlo.nodenet.serialization.Serializer;

/**
 * A serializable representation of a class that can be turned into the actual
 * class.
 */
public class SerializableClass<E> {

	public Serializer serializer(Serializer subSerializer) {
		return new FieldSerializer(SerializableClass.class, subSerializer, SerializableClass::new);
	}

	private String name;

	private SerializableClass() {
	}

	public SerializableClass(Class<E> clazz) {
		this.name = clazz.getName();
	}
	
	@SuppressWarnings("unchecked")
	public Class<E> toClass() throws ProtocolViolationException {
		try {
			return (Class<E>) Class.forName(name);
		} catch (ClassNotFoundException e) {
			throw new ProtocolViolationException(e);
		}
	}
	
}
