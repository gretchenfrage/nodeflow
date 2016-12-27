package com.phoenixkahlo.nodenet;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

	public AddressedMessageHandler(NodeAddress localAddress, NetworkModel model,
			Map<NodeAddress, ObjectStream> connections, Map<NodeAddress, ChildNode> nodes) {
		this.localAddress = localAddress;
		this.model = model;
		this.connections = connections;
		this.nodes = nodes;
	}

	public void handle(AddressedMessage message, NodeAddress from) {
		if (message.getDestination().equals(localAddress)) {
			ObjectStream stream;
			synchronized (connections) {
				stream = connections.get(from);
			}
			if (stream == null) {
				System.err.println("Stream not found sending AddressedMessageResult to " + from);
				return;
			}
			try {
				stream.send(new AddressedMessageResult(message.getOriginalTransmissionID(), true));
			} catch (DisconnectionException e) {
				System.err.println("DisconnectionException sending AddressedMessageResult to " + from);
			}
			handlePayload(message.getSender(), message.getPayload(), message.getMessageID());
		} else {
			new AddressedDelegatorThread(message, localAddress, model, connections, addressedResults, from).start();
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
					System.err.println("Failed to get client payload to node - node not found");
					return;
				}
				node.receiveFromParent(((ClientTransmission) payload).getObject());
			} else {
				System.err.println("Failed to handle AddressedMessagePayload: " + payload);
			}
		}
	}

}
