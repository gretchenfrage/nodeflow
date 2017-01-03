package com.phoenixkahlo.nodenet.proxy;

import java.util.List;
import java.util.Optional;

import com.phoenixkahlo.nodenet.AddressedPayload;
import com.phoenixkahlo.nodenet.NodeAddress;
import com.phoenixkahlo.nodenet.serialization.FieldSerializer;
import com.phoenixkahlo.nodenet.serialization.Serializer;
import com.phoenixkahlo.util.UUID;

public class ProxyMultiInvocation implements AddressedPayload {

	public static Serializer serializer(Serializer subSerializer) {
		return new FieldSerializer(ProxyMultiInvocation.class, subSerializer, ProxyMultiInvocation::new);
	}
	
	private UUID invocationID;
	private Optional<NodeAddress> returnAddress;
	private List<ProxyInvocation> invocations;

	public ProxyMultiInvocation() {
	}
	
	public ProxyMultiInvocation(UUID invocationID, Optional<NodeAddress> returnAddress,
			List<ProxyInvocation> invocations) {
		super();
		this.invocationID = invocationID;
		this.returnAddress = returnAddress;
		this.invocations = invocations;
	}

	public UUID getInvocationID() {
		return invocationID;
	}

	public Optional<NodeAddress> getReturnAddress() {
		return returnAddress;
	}

	public List<ProxyInvocation> getInvocations() {
		return invocations;
	}

}
