package com.phoenixkahlo.nodenet.proxy;

public class ProxyResult {

	public static enum Type {
		NORMAL, TARGETEXCEPTION, PROXYEXCEPTION
	}
	
	private Object result;
	private Type type;
	
	public ProxyResult(Object result, Type type) {
		this.result = result;
		this.type = type;
	}

	public Object getResult() {
		return result;
	}

	public Type getType() {
		return type;
	}
	
}
