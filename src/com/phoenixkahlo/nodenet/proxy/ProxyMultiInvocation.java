package com.phoenixkahlo.nodenet.proxy;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import com.phoenixkahlo.nodenet.AddressedPayload;
import com.phoenixkahlo.nodenet.NodeAddress;
import com.phoenixkahlo.nodenet.serialization.FieldSerializer;
import com.phoenixkahlo.nodenet.serialization.Serializer;

public class ProxyMultiInvocation implements AddressedPayload {

	public static Serializer serializer(Serializer subSerializer) {
		return new FieldSerializer(ProxyMultiInvocation.class, subSerializer, ProxyMultiInvocation::new);
	}
	
	private int invocationID;
	private List<ProxyInvocation> invocations;
	private Optional<NodeAddress> returnAddress;
	
	private ProxyMultiInvocation() {
	}
	
	public ProxyMultiInvocation(List<ProxyInvocation> invocations, Optional<NodeAddress> returnAddress) {
		this.invocationID = ThreadLocalRandom.current().nextInt();
		this.invocations = invocations;
		this.returnAddress = returnAddress;
	}
	
	public int getInvocationID() {
		return invocationID;
	}
	
	public List<ProxyInvocation> getInvocations() {
		return invocations;
	}
	
	public Optional<NodeAddress> getReturnAddress() {
		return returnAddress;
	}
	
}
