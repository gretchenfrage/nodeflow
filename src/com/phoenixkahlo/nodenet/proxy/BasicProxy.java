package com.phoenixkahlo.nodenet.proxy;

import java.lang.reflect.InvocationHandler;
import java.util.Optional;

import com.phoenixkahlo.nodenet.DisconnectionException;
import com.phoenixkahlo.nodenet.NodeAddress;
import com.phoenixkahlo.nodenet.serialization.FieldSerializer;
import com.phoenixkahlo.nodenet.serialization.Serializer;

public class BasicProxy<E> implements Proxy<E> {

	public static Serializer serializer(Serializer subSerializer, ProxyHandler proxyHandler, NodeAddress localAddress) {
		return new FieldSerializer(BasicProxy.class, subSerializer, () -> new BasicProxy<Object>(proxyHandler, localAddress));
	}

	private int proxyID;
	private NodeAddress source;
	private Class<E> implementing;
	private transient ProxyHandler proxyHandler;
	private transient NodeAddress localAddress;

	private BasicProxy(ProxyHandler handler, NodeAddress localAddress) {
		this.proxyHandler = handler;
		this.localAddress = localAddress;
	}

	public BasicProxy(int proxyID, NodeAddress source, Class<E> implementing, ProxyHandler proxyHandler,
			NodeAddress localAddress) {
		this.proxyID = proxyID;
		this.source = source;
		this.implementing = implementing;
		this.proxyHandler = proxyHandler;
		this.localAddress = localAddress;
	}

	@SuppressWarnings("unchecked")
	@Override
	public E blocking() {
		InvocationHandler invocationHandler = (proxy, method, args) -> {
			ProxyResult result;
			try {
				result = proxyHandler
						.sendAndWait(new ProxyInvocation(proxyID, method, args, Optional.of(localAddress)), source);
			} catch (DisconnectionException e) {
				throw new RuntimeDisconnectionException(e);
			}
			switch (result.getType()) {
			case NORMAL:
				return result.getResult();
			case TARGETEXCEPTION:
				throw (Throwable) result.getResult();
			case PROXYEXCEPTION:
				throw new RuntimeProxyException();
			default:
				throw new RuntimeException();	
			}
		};
		return (E) java.lang.reflect.Proxy.newProxyInstance(BasicProxy.class.getClassLoader(),
				new Class<?>[] { implementing }, invocationHandler);
	}
	
	private static Object getGibberish(Class<?> type) {
		if (type.equals(int.class))
			return new Integer(0);
		else if (type.equals(long.class))
			return new Long(0);
		else if (type.equals(float.class))
			return new Float(0);
		else if (type.equals(double.class))
			return new Double(0);
		else if (type.equals(short.class))
			return new Short((short) 0);
		else if (type.equals(char.class))
			return new Character((char) 0);
		else if (type.equals(byte.class))
			return new Byte((byte) 0);
		else if (type.equals(boolean.class))
			return Boolean.FALSE;
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public E unblocking(boolean disconnectionException) {
		InvocationHandler invocationHandler = (proxy, method, args) -> {
			try {
				proxyHandler.sendAndWait(new ProxyInvocation(proxyID, method, args, Optional.empty()), source);
			} catch (DisconnectionException e) {
				if (disconnectionException)
					throw new RuntimeDisconnectionException(e);
			}
			
			return getGibberish(method.getReturnType());
		};
		return (E) java.lang.reflect.Proxy.newProxyInstance(BasicProxy.class.getClassLoader(),
				new Class<?>[] { implementing }, invocationHandler);
	}

	@Override
	public NodeAddress getSource() {
		return source;
	}

	@Override
	public int getProxyID() {
		return proxyID;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Proxy<T> cast(Class<T> to) {
		if (to.isAssignableFrom(implementing))
			return (Proxy<T>) this;
		else
			throw new ClassCastException();
	}

}
