package com.phoenixkahlo.nodenet.proxy;

import java.lang.reflect.InvocationHandler;
import java.util.Optional;

import com.phoenixkahlo.nodenet.DisconnectionException;
import com.phoenixkahlo.nodenet.NodeAddress;
import com.phoenixkahlo.nodenet.serialization.FieldSerializer;
import com.phoenixkahlo.nodenet.serialization.Serializer;
import com.phoenixkahlo.util.UUID;

public class BasicProxy<E> implements Proxy<E> {

	public static Serializer serializer(Serializer subSerializer, ProxyHandler proxyHandler, NodeAddress localAddress) {
		return new FieldSerializer(BasicProxy.class, subSerializer,
				() -> new BasicProxy<Object>(proxyHandler, localAddress));
	}

	private UUID proxyID;
	private NodeAddress source;
	private Class<E> implementing;
	private transient ProxyHandler proxyHandler;
	private transient NodeAddress localAddress;

	private BasicProxy(ProxyHandler handler, NodeAddress localAddress) {
		this.proxyHandler = handler;
		this.localAddress = localAddress;
	}

	public BasicProxy(UUID proxyID, NodeAddress source, Class<E> implementing, ProxyHandler proxyHandler,
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
			result = proxyHandler.sendAndWait(new ProxyInvocation(proxyID, method, args, Optional.of(localAddress)),
					source);
			switch (result.getType()) {
			case NORMAL:
				return result.getResult();
			case TARGETEXCEPTION:
				throw (Throwable) result.getResult();
			case PROXYEXCEPTION:
				throw new RuntimeProxyException();
			case DISCONNECTIONEXCEPTION:
				throw new RuntimeDisconnectionException();
			default:
				throw new RuntimeException();
			}
		};
		return (E) java.lang.reflect.Proxy.newProxyInstance(BasicProxy.class.getClassLoader(),
				new Class<?>[] { implementing }, invocationHandler);
	}

	private static Object getGibberish(Class<?> type) {
		/*
		 * Some people credit the etymology of "gibberish" to the
		 * incomprehensible works of 8th century alchemist Jabir ibn Hayyan.
		 */
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
				proxyHandler.sendDontWait(new ProxyInvocation(proxyID, method, args, Optional.empty()), source);
			} catch (DisconnectionException e) {
				if (disconnectionException)
					throw new RuntimeDisconnectionException(e);
			}

			return getGibberish(method.getReturnType());
		};
		return (E) java.lang.reflect.Proxy.newProxyInstance(BasicProxy.class.getClassLoader(),
				new Class<?>[] { implementing }, invocationHandler);
	}

	@SuppressWarnings("unchecked")
	@Override
	public E buffered(InvocationBuffer buffer) {
		InvocationHandler invocationHandler = (proxy, method, args) -> {
			ProxyInvocation invocation = new ProxyInvocation(proxyID, method, args, Optional.empty());
			buffer.buffer(invocation, source, proxyHandler, localAddress);
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
	public UUID getProxyID() {
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
	
	@Override
	public String toString() {
		return "proxy<" + implementing.getSimpleName() + ">";
	}

}
