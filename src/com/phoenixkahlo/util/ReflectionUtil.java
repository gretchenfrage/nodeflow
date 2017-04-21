package com.phoenixkahlo.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReflectionUtil {

	private ReflectionUtil() {}
	
	public static List<Field> getAllFields(Class<?> clazz) {
		if (clazz == Object.class) {
			return new ArrayList<>();
		} else {
			List<Field> fields = getAllFields(clazz.getSuperclass());
			fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
			return fields;
		}
	}
	
}
