package com.phoenixkahlo.nodenet.proxy;

import com.phoenixkahlo.nodenet.AddressedPayload;
import com.phoenixkahlo.nodenet.serialization.FieldSerializer;
import com.phoenixkahlo.nodenet.serialization.Serializer;
import com.phoenixkahlo.util.UUID;

public class ProxyResult implements AddressedPayload {

	public static Serializer serializer(Serializer subSerializer) {
		return new FieldSerializer(ProxyResult.class, subSerializer, ProxyResult::new);
	}
	
	public static enum Type {
		NORMAL, TARGETEXCEPTION, PROXYEXCEPTION, DISCONNECTIONEXCEPTION
	}
	
	private UUID invocationID;
	private Object result;
	private Type type;
	
	private ProxyResult() {
	}
	
	public ProxyResult(UUID invocationID, Object result, Type type) {
		this.invocationID = invocationID;
		this.result = result;
		this.type = type;
	}
	
	public UUID getInvocationID() {
		return invocationID;
	}

	public Object getResult() {
		return result;
	}

	public Type getType() {
		return type;
	}
	
}
