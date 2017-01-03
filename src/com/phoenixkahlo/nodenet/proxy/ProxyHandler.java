package com.phoenixkahlo.nodenet.proxy;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

import com.phoenixkahlo.nodenet.AddressedMessageHandler;
import com.phoenixkahlo.nodenet.DisconnectionException;
import com.phoenixkahlo.nodenet.Node;
import com.phoenixkahlo.nodenet.NodeAddress;
import com.phoenixkahlo.util.BlockingHashMap;
import com.phoenixkahlo.util.BlockingMap;

public class ProxyHandler {

	private AddressedMessageHandler addressedHandler;
	private Map<Integer, Object> sources = Collections.synchronizedMap(new HashMap<>());
	private NodeAddress localAddress;
	private Function<NodeAddress, Optional<Node>> nodeLookup;
	private BlockingMap<Integer, ProxyResult> results = new BlockingHashMap<>();

	public ProxyHandler(AddressedMessageHandler addressedHandler, NodeAddress localAddress,
			Function<NodeAddress, Optional<Node>> nodeLookup) {
		this.addressedHandler = addressedHandler;
		this.localAddress = localAddress;
		this.nodeLookup = nodeLookup;
	}

	public <E> Proxy<E> makeProxy(E source, Class<E> intrface) {
		int id = ThreadLocalRandom.current().nextInt();
		sources.put(id, source);
		return new BasicProxy<>(id, localAddress, intrface, this, localAddress);
	}

	public void removeProxy(Object source) {
		synchronized (sources) {
			for (Object key : sources.keySet()) {
				if (sources.get(key).equals(source))
					sources.remove(key);
			}
		}
	}

	public void removeProxy(Proxy<?> proxy) {
		sources.remove(proxy.getProxyID());
	}

	public void handle(ProxyInvocation invocation) {
		Object source = sources.get(invocation.getProxyID());
		if (source == null) {
			if (invocation.getReturnAddress().isPresent())
				addressedHandler.send(
						new ProxyResult(invocation.getInvocationID(), null, ProxyResult.Type.PROXYEXCEPTION),
						invocation.getReturnAddress().get());
			return;
		}
		if (invocation.getReturnAddress().isPresent()) {
			new Thread(() -> {
				try {
					Object result = invocation.getMethod().invoke(source, invocation.getArgs());
					addressedHandler.send(
							new ProxyResult(invocation.getInvocationID(), result, ProxyResult.Type.NORMAL),
							invocation.getReturnAddress().get());
				} catch (InvocationTargetException e) {
					addressedHandler.send(new ProxyResult(invocation.getInvocationID(),
							((InvocationTargetException) e).getTargetException(), ProxyResult.Type.TARGETEXCEPTION),
							invocation.getReturnAddress().get());
				} catch (IllegalAccessException | IllegalArgumentException e) {
					addressedHandler.send(
							new ProxyResult(invocation.getInvocationID(), null, ProxyResult.Type.PROXYEXCEPTION),
							invocation.getReturnAddress().get());
				}
			}).start();
		} else {
			new Thread(() -> {
				try {
					invocation.getMethod().invoke(source, invocation.getArgs());
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				}
			}).start();
		}
	}

	public void handle(ProxyMultiInvocation multiInvocation) {
		new Thread(() -> {
			boolean proxyException = false;
			for (ProxyInvocation invocation : multiInvocation.getInvocations()) {
				Object source = sources.get(invocation.getProxyID());
				if (source == null) {
					proxyException = true;
				} else {
					try {
						invocation.getMethod().invoke(source, invocation.getArgs());
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						proxyException = true;
					}
				}
			}
			if (multiInvocation.getReturnAddress().isPresent()) {
				if (proxyException)
					addressedHandler.send(
							new ProxyResult(multiInvocation.getInvocationID(), null, ProxyResult.Type.PROXYEXCEPTION),
							multiInvocation.getReturnAddress().get());
				else
					addressedHandler.send(
							new ProxyResult(multiInvocation.getInvocationID(), null, ProxyResult.Type.NORMAL),
							multiInvocation.getReturnAddress().get());
			}
		}).start();
	}

	public void handle(ProxyResult result) {
		results.put(result.getInvocationID(), result);
	}

	public ProxyResult sendAndWait(ProxyInvocation invocation, NodeAddress to) {
		Optional<Node> node = nodeLookup.apply(to);
		if (node.isPresent() && !node.get().isDisconnected())
			node.get().listenForDisconnect(() -> results.put(invocation.getInvocationID(),
					new ProxyResult(invocation.getInvocationID(), null, ProxyResult.Type.DISCONNECTIONEXCEPTION)));
		else
			return new ProxyResult(invocation.getInvocationID(), null, ProxyResult.Type.DISCONNECTIONEXCEPTION);

		if (addressedHandler.sendAndWait(invocation, to))
			return results.get(invocation.getInvocationID());
		else if (node.isPresent() && node.get().isDisconnected())
			return new ProxyResult(invocation.getInvocationID(), null, ProxyResult.Type.DISCONNECTIONEXCEPTION);
		else
			return new ProxyResult(invocation.getInvocationID(), null, ProxyResult.Type.PROXYEXCEPTION);
	}

	public void sendDontWait(ProxyInvocation invocation, NodeAddress to) throws DisconnectionException {
		Optional<Node> node = nodeLookup.apply(to);
		if (!node.isPresent() || node.get().isDisconnected())
			throw new DisconnectionException();
		addressedHandler.send(invocation, to);
	}

	public void sendAndWait(ProxyMultiInvocation multiInvocation, NodeAddress to)
			throws DisconnectionException, ProxyException {
		Optional<Node> node = nodeLookup.apply(to);
		if (node.isPresent() && !node.get().isDisconnected())
			node.get().listenForDisconnect(() -> results.put(multiInvocation.getInvocationID(),
					new ProxyResult(multiInvocation.getInvocationID(), null, ProxyResult.Type.DISCONNECTIONEXCEPTION)));
		else
			throw new DisconnectionException();

		if (addressedHandler.sendAndWait(multiInvocation, to)) {
			ProxyResult result = results.get(multiInvocation.getInvocationID());
			if (result.getType() == ProxyResult.Type.PROXYEXCEPTION)
				throw new ProxyException();
		} else if (node.isPresent() && node.get().isDisconnected()) {
			throw new DisconnectionException();
		} else {
			throw new ProxyException();
		}
	}

	public void sendDontWait(ProxyMultiInvocation multiInvocation, NodeAddress to) throws DisconnectionException {
		Optional<Node> node = nodeLookup.apply(to);
		if (!node.isPresent() || node.get().isDisconnected())
			throw new DisconnectionException();
		addressedHandler.send(multiInvocation, to);
	}

}
