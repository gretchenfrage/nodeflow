package com.phoenixkahlo.nodenet;

<<<<<<< HEAD
import java.util.Map;

import com.phoenixkahlo.nodenet.stream.DisconnectionException;
import com.phoenixkahlo.nodenet.stream.ObjectStream;
import com.phoenixkahlo.util.BlockingHashMap;
import com.phoenixkahlo.util.BlockingMap;

=======
>>>>>>> eb56286c0399094b26770a91c1ceb3d22c73ee44
/**
 * An object owned by a LocalNode to handle the AddressedMessage system.
 */
public class AddressedMessageHandler {

<<<<<<< HEAD
	private NodeAddress localAddress;
	private NetworkModel model;
	private Map<NodeAddress, ObjectStream> connections;
	private Map<NodeAddress, ChildNode> nodes;
	private BlockingMap<Integer, Boolean> addressedResults = new BlockingHashMap<>();

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
			handlePayload(from, message.getPayload());
		} else {
			new AddressedDelegatorThread(message, localAddress, model, connections, addressedResults, from).start();
		}
	}

	public void handle(AddressedMessageResult message) {
		addressedResults.put(message.getTransmissionID(), message.wasSuccessful());
	}

	private void handlePayload(NodeAddress from, AddressedPayload payload) {
		if (payload instanceof ClientTransmission) {
			ChildNode node;
			synchronized (nodes) {
				node = nodes.get(from);
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

=======
	public void start() {
		
	}
	
	public void stop() {
		
	}
	
>>>>>>> eb56286c0399094b26770a91c1ceb3d22c73ee44
}
