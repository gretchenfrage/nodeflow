package com.phoenixkahlo.nodenet.proxy;

import java.util.List;
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
	private SerializableMethod method;
	private List<Object> args;
	private Optional<NodeAddress> returnAddress;
	
	private ProxyInvocation() {
	}
	
	public ProxyInvocation(int proxyID, SerializableMethod method, List<Object> args, Optional<NodeAddress> returnAddress) {
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
	
	public SerializableMethod getMethod() {
		return method;
	}
	
	public List<Object> getArgs() {
		return args;
	}
	
	public Optional<NodeAddress> getReturnAddress() {
		return returnAddress;
	}

	
}
