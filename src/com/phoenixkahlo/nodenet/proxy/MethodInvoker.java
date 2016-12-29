package com.phoenixkahlo.nodenet.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

import javax.management.ReflectionException;

public class MethodInvoker {

	private String methodName;
	private List<String> argTypes;
	private List<Object> args;
	
	public MethodInvoker(Class<?> clazz, String methodName, List<Class<?>> argTypes, List<Object> args) throws NoSuchMethodException {
		clazz.getMethod(methodName, argTypes.toArray(new Class<?>[argTypes.size()]));
		this.methodName = methodName;
		this.argTypes = argTypes.stream().map(Class::getName).collect(Collectors.toList());
		this.args = args;
	}
	
	public Object invoke(Object object) throws ReflectionException, InvocationTargetException {
		try {
			Method method = object.getClass().getMethod(methodName, argTypes.stream().map(type -> {
				try {
					return Class.forName(type);
				} catch (ClassNotFoundException e) {
					return null;
				}
			}).toArray(Class[]::new));
			return method.invoke(object, args);
		} catch (InvocationTargetException e) {
			throw e;
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException e) {
			throw new ReflectionException(e);
		}
	}
	
}
