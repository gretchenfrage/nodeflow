package com.phoenixkahlo.nodenet.proxy;

import java.lang.reflect.Method;
import java.util.Optional;

import com.phoenixkahlo.nodenet.AddressedPayload;
import com.phoenixkahlo.nodenet.NodeAddress;
import com.phoenixkahlo.nodenet.serialization.FieldSerializer;
import com.phoenixkahlo.nodenet.serialization.Serializer;
import com.phoenixkahlo.util.UUID;

public class ProxyInvocation implements AddressedPayload {

	public static Serializer serializer(Serializer subSerializer) {
		return new FieldSerializer(ProxyInvocation.class, subSerializer, ProxyInvocation::new);
	}
	
	private UUID proxyID;
	private UUID invocationID;
	private Method method;
	private Object[] args;
	private Optional<NodeAddress> returnAddress;
	
	private ProxyInvocation() {
	}
	
	public ProxyInvocation(UUID proxyID, Method method, Object[] args, Optional<NodeAddress> returnAddress) {
		this.proxyID = proxyID;
		this.invocationID = new UUID();
		this.method = method;
		this.args = args;
		this.returnAddress = returnAddress;
	}
	
	public UUID getProxyID() {
		return proxyID;
	}
	
	public UUID getInvocationID() {
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
