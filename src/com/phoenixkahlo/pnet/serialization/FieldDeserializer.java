package com.phoenixkahlo.pnet.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Supplier;

import com.phoenixkahlo.pnet.ProtocolViolationException;
import com.phoenixkahlo.util.ReflectionUtil;

public class FieldDeserializer implements Deserializer {

	private Class<?> dataType;
	private Deserializer subDeserializer;
	private Supplier<?> factory;
	
	public <E> FieldDeserializer(Class<E> dataType, Deserializer subDeserializer, Supplier<E> factory) {
		this.dataType = dataType;
		this.subDeserializer = subDeserializer;
		this.factory = factory;
	}
	
	public <E> FieldDeserializer(Class<E> dataType, Supplier<E> factory) {
		this(dataType, null, factory);
	}
	
	FieldDeserializer(Supplier<?> factory, Deserializer subDeserializer, Class<?> dataType) {
		assert factory.get().getClass() == dataType;
		this.dataType = dataType;
		this.subDeserializer = subDeserializer;
		this.factory = factory;
	}
	
	/**
	 * Abandon all hope, ye who enter here.
	 */
	@Deprecated
	public FieldDeserializer(Class<?> dataType) {
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
	public Object deserialize(InputStream in) throws IOException, ProtocolViolationException {
		Object object = factory.get();
		
		for (Field field : ReflectionUtil.getAllFields(dataType)) {
			field.setAccessible(true);
			if (!Modifier.isTransient(field.getModifiers())) {
				try {
					Object value = SerializationUtils.deserialize(field.getType(), subDeserializer, in);
					field.set(object, value);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		}
		
		if (object instanceof AutoSerializer)
			((AutoSerializer) object).autoDeserialize(in);
		
		return object;
	}

	@Override
	public Serializer toSerializer() {
		return new FieldSerializer(factory, subDeserializer == null ? null : subDeserializer.toSerializer(), dataType);
	}

}
