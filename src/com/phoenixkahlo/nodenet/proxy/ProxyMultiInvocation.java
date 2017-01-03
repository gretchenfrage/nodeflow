package com.phoenixkahlo.nodenet.proxy;

import java.util.List;
import java.util.Optional;

import com.phoenixkahlo.nodenet.AddressedPayload;
import com.phoenixkahlo.nodenet.NodeAddress;
import com.phoenixkahlo.nodenet.serialization.FieldSerializer;
import com.phoenixkahlo.nodenet.serialization.Serializer;

public class ProxyMultiInvocation implements AddressedPayload {

	public static Serializer serializer(Serializer subSerializer) {
		return new FieldSerializer(ProxyMultiInvocation.class, subSerializer, ProxyMultiInvocation::new);
	}
	
	private int invocationID;
	private Optional<NodeAddress> returnAddress;
	private List<ProxyInvocation> invocations;

	public ProxyMultiInvocation() {
	}
	
	public ProxyMultiInvocation(int invocationID, Optional<NodeAddress> returnAddress,
			List<ProxyInvocation> invocations) {
		super();
		this.invocationID = invocationID;
		this.returnAddress = returnAddress;
		this.invocations = invocations;
	}

	public int getInvocationID() {
		return invocationID;
	}

	public Optional<NodeAddress> getReturnAddress() {
		return returnAddress;
	}

	public List<ProxyInvocation> getInvocations() {
		return invocations;
	}

}
