package com.phoenixkahlo.nodenet.proxy;

import java.util.HashMap;
import java.util.Map;

import com.phoenixkahlo.nodenet.AddressedMessageHandler;
import com.phoenixkahlo.nodenet.DisconnectionException;
import com.phoenixkahlo.nodenet.NodeAddress;

public class ProxyHandler {

	private AddressedMessageHandler addressedHandler;
	private Map<Integer, Object> sources = new HashMap<>();
	
	public ProxyHandler(AddressedMessageHandler addressedHandler) {
		this.addressedHandler = addressedHandler;
	}
	
	public ProxyResult sendAndWait(ProxyInvocation invocation, NodeAddress to) throws DisconnectionException {
		
	}
	
	public void sendDontWait(ProxyInvocation invocation, NodeAddress to) throws DisconnectionException {
		
	}
	
}
