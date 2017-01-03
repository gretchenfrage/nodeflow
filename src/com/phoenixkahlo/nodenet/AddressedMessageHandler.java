package com.phoenixkahlo.nodenet;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import com.phoenixkahlo.nodenet.proxy.ProxyHandler;
import com.phoenixkahlo.nodenet.proxy.ProxyInvocation;
import com.phoenixkahlo.nodenet.proxy.ProxyMultiInvocation;
import com.phoenixkahlo.nodenet.proxy.ProxyResult;
import com.phoenixkahlo.nodenet.stream.ObjectStream;
import com.phoenixkahlo.util.BlockingHashMap;
import com.phoenixkahlo.util.BlockingMap;

/**
 * An object owned by a LocalNode to handle the AddressedMessage system. When a
 * StreamReceiverThread receives an AddressedMessage or AddressedMessageResult,
 * it is delegated to the AddressedMessageHandler, which will be responsible for
 * handling the payload/sending it to a neighbor, and responding with an
 * AddressedPayloadResult to the neighbor who sent it.
 */
public class AddressedMessageHandler {

	private NodeAddress localAddress;
	private NetworkModel model;
	private Map<NodeAddress, ObjectStream> connections;
	private Map<NodeAddress, ChildNode> nodes;
	private BlockingMap<Integer, Boolean> addressedResults = new BlockingHashMap<>();
	private Set<Integer> handledPayloadMessageIDs = new HashSet<>();
	private ProxyHandler proxyHandler;

	private PrintStream errorLog;

	public AddressedMessageHandler(NodeAddress localAddress, NetworkModel model,
			Map<NodeAddress, ObjectStream> connections, Map<NodeAddress, ChildNode> nodes, PrintStream errorLog) {
		this.localAddress = localAddress;
		this.model = model;
		this.connections = connections;
		this.nodes = nodes;
		this.errorLog = errorLog;
	}

	// TODO: make a more elegant solution to this
	public void setProxyHandler(ProxyHandler proxyHandler) {
		this.proxyHandler = proxyHandler;
	}

	public void send(AddressedPayload payload, NodeAddress to) {
		handle(new AddressedMessage(payload, localAddress, to), localAddress);
	}

	public void send(AddressedPayload payload, NodeAddress to, Consumer<Boolean> resultHandler) {
		handle(new AddressedMessage(payload, localAddress, to), localAddress, resultHandler);
	}

	public boolean sendAndWait(AddressedPayload payload, NodeAddress to) {
		BlockingQueue<Boolean> result = new LinkedBlockingQueue<>();
		send(payload, to, result::add);
		try {
			return result.take();
		} catch (InterruptedException e) {
			errorLog.println("Interrupted while waiting for result of send attempt.");
			return false;
		}
	}

	public void handle(AddressedMessage message, NodeAddress from) {
		handle(message, from, result -> {
		});
	}

	public void handle(AddressedMessage message, NodeAddress from, Consumer<Boolean> resultHandler) {
		if (message.getDestination().equals(localAddress)) {
			ObjectStream stream;
			synchronized (connections) {
				stream = connections.get(from);
			}
			if (stream == null) {
				errorLog.println("Stream not found sending AddressedMessageResult to " + from);
				return;
			}
			try {
				stream.send(new AddressedMessageResult(message.getOriginalTransmissionID(), true));
			} catch (DisconnectionException e) {
				errorLog.println("DisconnectionException sending AddressedMessageResult to " + from);
			}
			handlePayload(message.getSender(), message.getPayload(), message.getMessageID());
		} else {
			new AddressedDelegatorThread(message, localAddress, model, connections, addressedResults, from, errorLog,
					resultHandler).start();
		}
	}

	public void handle(AddressedMessageResult message) {
		addressedResults.put(message.getTransmissionID(), message.wasSuccessful());
	}

	private void handlePayload(NodeAddress sender, AddressedPayload payload, int messageID) {
		boolean shouldHandle;
		synchronized (handledPayloadMessageIDs) {
			if (handledPayloadMessageIDs.contains(messageID)) {
				shouldHandle = false;
			} else {
				shouldHandle = true;
				handledPayloadMessageIDs.add(messageID);
			}
		}
		if (shouldHandle) {
			if (payload instanceof ClientTransmission) {
				ChildNode node;
				synchronized (nodes) {
					node = nodes.get(sender);
				}
				if (node == null) {
					errorLog.println("Failed to get client payload to node - node not found");
					return;
				}
				node.receiveFromParent(((ClientTransmission) payload).getObject());
			} else if (payload instanceof ProxyInvocation) {
				proxyHandler.handle((ProxyInvocation) payload);
			} else if (payload instanceof ProxyResult) {
				proxyHandler.handle((ProxyResult) payload);
			} else if (payload instanceof ProxyMultiInvocation) {
				proxyHandler.handle((ProxyMultiInvocation) payload);
			} else {
				errorLog.println("Failed to handle AddressedMessagePayload: " + payload);
			}
		}
	}

}
