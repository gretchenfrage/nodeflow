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
	
	public FieldDeserializer(Class<?> dataType, Deserializer subDeserializer, Supplier<?> factory) {
		assert factory.get().getClass() == dataType;
		this.dataType = dataType;
		this.subDeserializer = subDeserializer;
		this.factory = factory;
	}
	
	public FieldDeserializer(Class<?> dataType, Supplier<?> factory) {
		this(dataType, null, factory);
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
		
		if (object instanceof AutoDeserializer)
			((AutoDeserializer) object).autoDeserialize(in);
		
		return object;
	}

	@Override
	public Serializer toSerializer() {
		if (subDeserializer == null)
			return new FieldSerializer(dataType, factory);
		else
			return new FieldSerializer(dataType, subDeserializer.toSerializer(), factory);
	}

}
