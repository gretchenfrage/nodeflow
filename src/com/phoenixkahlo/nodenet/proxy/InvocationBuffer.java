package com.phoenixkahlo.nodenet.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.phoenixkahlo.nodenet.DisconnectionException;
import com.phoenixkahlo.nodenet.NodeAddress;
import com.phoenixkahlo.util.UUID;

public class InvocationBuffer {

	private List<ProxyInvocation> invocations = new ArrayList<>();
	// source and proxyHandler are provided by proxies upon buffering
	private NodeAddress source;
	private ProxyHandler proxyHandler;
	private NodeAddress localAddress;

	public synchronized void buffer(ProxyInvocation invocation, NodeAddress source, ProxyHandler proxyHandler,
			NodeAddress localAddress) {
		if (this.source == null) {
			this.source = source;
			this.proxyHandler = proxyHandler;
			this.localAddress = localAddress;
		} else {
			if (!this.source.equals(source))
				throw new IllegalStateException("Discrepant sources in InvocationBuffer");
		}
		invocations.add(invocation);
	}

	public synchronized void flushAndWait() throws DisconnectionException, ProxyException {
		if (invocations.isEmpty())
			return;

		UUID invocationID = new UUID();
		ProxyMultiInvocation multiInvocation = new ProxyMultiInvocation(invocationID, Optional.of(localAddress),
				invocations);
		proxyHandler.sendAndWait(multiInvocation, source);
	}

	public synchronized void flushDontWait() throws DisconnectionException {
		if (invocations.isEmpty())
			return;

		UUID invocationID = new UUID();
		ProxyMultiInvocation multiInvocation = new ProxyMultiInvocation(invocationID, Optional.empty(), invocations);
		proxyHandler.sendDontWait(multiInvocation, source);
	}

}
