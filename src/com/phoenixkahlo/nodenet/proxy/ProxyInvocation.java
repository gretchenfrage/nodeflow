package com.phoenixkahlo.nodenet.proxy;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import javax.management.ReflectionException;

import com.phoenixkahlo.nodenet.NodeAddress;

public class ProxyInvocation {

	private int invocationID;
	private NodeAddress returnAddress; // Nullable
	private MethodInvoker methodInvoker;
	
	public ProxyInvocation(MethodInvoker methodInvoker, Optional<NodeAddress> returnAddress) {
		this.invocationID = ThreadLocalRandom.current().nextInt();
		this.returnAddress = returnAddress.orElseGet(() -> null);
		this.methodInvoker = methodInvoker;
	}
	
	public int getInvocationID() {
		return invocationID;
	}
	
	public Optional<NodeAddress> getReturnAddress() {
		return Optional.ofNullable(returnAddress);
	}
	
	public Object invoke(Object object) throws ReflectionException, InvocationTargetException {
		return methodInvoker.invoke(object);
	}
	
}
