package com.phoenixkahlo.pnet.serialization;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Supplier;

import com.phoenixkahlo.util.ReflectionUtil;

public class FieldSerializer implements Serializer {

	private Class<?> dataType;
	private Serializer subSerializer;
	private Supplier<?> factory;

	public <E> FieldSerializer(Class<E> dataType, Serializer subSerializer, Supplier<E> factory) {
		this.dataType = dataType;
		this.subSerializer = subSerializer;
		this.factory = factory;
	}

	public <E> FieldSerializer(Class<E> dataType, Supplier<E> factory) {
		this(dataType, null, factory);
	}

	FieldSerializer(Supplier<?> factory, Serializer subSerializer, Class<?> dataType) {
		assert factory.get().getClass() == dataType;
		this.dataType = dataType;
		this.factory = factory;
		this.subSerializer = subSerializer;
	}

	/**
	 * Abandon all hope, ye who enter here.
	 */
	@Deprecated
	public FieldSerializer(Class<?> dataType) {
		this.dataType = dataType;
		this.factory = () -> {
			try {
				Class<?> unsafeClass = FieldSerializer.class.getClassLoader().loadClass("sun.misc.Unsafe");
				Field theUnsafeField = unsafeClass.getField("theUnsafe");
				theUnsafeField.setAccessible(true);
				Object theUnsafe = theUnsafeField.get(null);
				Method allocateInstance = unsafeClass.getMethod("allocateInstance", Class.class);
				Object straightOuttaTheOperatingSystem = allocateInstance.invoke(theUnsafe, dataType);
				return straightOuttaTheOperatingSystem;
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		};
	}

	@Override
	public boolean canSerialize(Object object) {
		return object.getClass() == dataType;
	}

	@Override
	public void serialize(Object object, OutputStream out) throws IOException {
		if (!canSerialize(object))
			throw new IllegalArgumentException();

		for (Field field : ReflectionUtil.getAllFields(dataType)) {
			field.setAccessible(true);
			if (!Modifier.isTransient(field.getModifiers())) {
				try {
					SerializationUtils.serialize(field.get(object), field.getType(), subSerializer, out);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		}

		if (object instanceof AutoSerializer)
			((AutoSerializer) object).autoSerialize(out);
	}

	@Override
	public Deserializer toDeserializer() {
		return new FieldDeserializer(factory, subSerializer == null ? null : subSerializer.toDeserializer(), dataType);
	}

}
