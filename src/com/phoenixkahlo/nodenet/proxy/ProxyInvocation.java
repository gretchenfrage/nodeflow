package com.phoenixkahlo.nodenet.proxy;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import com.phoenixkahlo.nodenet.AddressedPayload;
import com.phoenixkahlo.nodenet.NodeAddress;
import com.phoenixkahlo.nodenet.serialization.FieldSerializer;
import com.phoenixkahlo.nodenet.serialization.Serializer;

public class ProxyInvocation implements AddressedPayload {

	public static Serializer serializer(Serializer subSerializer) {
		return new FieldSerializer(ProxyInvocation.class, subSerializer, ProxyInvocation::new);
	}
	
	private int proxyID;
	private int invocationID;
	private Method method;
	private Object[] args;
	private Optional<NodeAddress> returnAddress;
	
	private ProxyInvocation() {
	}
	
	public ProxyInvocation(int proxyID, Method method, Object[] args, Optional<NodeAddress> returnAddress) {
		this.proxyID = proxyID;
		this.invocationID = ThreadLocalRandom.current().nextInt();
		this.method = method;
		this.args = args;
		this.returnAddress = returnAddress;
	}
	
	public int getProxyID() {
		return proxyID;
	}
	
	public int getInvocationID() {
		return invocationID;
	}
	
	public Method getMethod() {
		return method;
	}
	
	public Object[] getArgs() {
		return args;
	}
	
	public Optional<NodeAddress> getReturnAddress() {
		return returnAddress;
	}

	
}
