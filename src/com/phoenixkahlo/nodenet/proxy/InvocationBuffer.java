package com.phoenixkahlo.nodenet.proxy;

import java.util.ArrayList;
import java.util.List;

import com.phoenixkahlo.nodenet.DisconnectionException;
import com.phoenixkahlo.nodenet.NodeAddress;

/**
 * Described in package info.
 */
public class InvocationBuffer {

	private NodeAddress source;
	private List<ProxyInvocation> invocations = new ArrayList<>();
	private ProxyHandler proxyHandler; // Injected by proxies upon buffering

	public InvocationBuffer(NodeAddress source) {
		this.source = source;
	}

	public synchronized void buffer(ProxyInvocation invocation, NodeAddress source, ProxyHandler handler)
			throws IllegalArgumentException {
		if (this.proxyHandler == null)
			this.proxyHandler = handler;

		if (!this.source.equals(source))
			throw new IllegalArgumentException("Discrepant sources in InvocationBuffer");

		invocations.add(invocation);
	}

	public synchronized void flushAndWait() throws DisconnectionException, ProxyException {
		if (invocations.isEmpty())
			return;
		
	}

	public synchronized void flushDontWait() throws DisconnectionException {
		if (invocations.isEmpty())
			return;
	}

}
