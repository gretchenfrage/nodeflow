package com.phoenixkahlo.nodenet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.phoenixkahlo.nodenet.stream.DisconnectionException;
import com.phoenixkahlo.nodenet.stream.ObjectStream;

/**
 * An object owned by a LocalNode to handle the ViralMessage system. When a
 * StreamReceiverThread receives a ViralMessage, it is delegated to the
 * ViralMessageHandler. The ViralMessageHandler handles the payload/sends it to
 * neighbors as appropriate.
 */
public class ViralMessageHandler {

	private NodeAddress localAddress;
	private Set<Integer> handled = new HashSet<>();
	private Map<NodeAddress, ObjectStream> connections;
	private Map<NodeAddress, ChildNode> nodes;
	private NetworkModel model;
	private AddressedMessageHandler addressedHandler;
	private List<Consumer<Node>> joinListeners;
	private List<Consumer<Node>> leaveListeners;

	public ViralMessageHandler(NodeAddress localAddress, Map<NodeAddress, ObjectStream> connections,
			Map<NodeAddress, ChildNode> nodes, NetworkModel model, AddressedMessageHandler addressedHandler,
			List<Consumer<Node>> joinListeners, List<Consumer<Node>> leaveListeners) {
		this.localAddress = localAddress;
		this.connections = connections;
		this.nodes = nodes;
		this.model = model;
		this.addressedHandler = addressedHandler;
		this.joinListeners = joinListeners;
		this.leaveListeners = leaveListeners;
	}

	/**
	 * Have all nodes handle the payload, including this one.
	 */
	public void transmit(ViralPayload payload) {
		handle(new ViralMessage(payload));
	}

	public void handle(ViralMessage message) {
		boolean handled;
		synchronized (this.handled) {
			handled = this.handled.contains(message.getID());
			this.handled.add(message.getID());
		}
		if (!handled) {
			message.addInfected(localAddress);
			synchronized (connections) {
				for (NodeAddress address : connections.keySet()) {
					if (!message.getInfected().contains(address)) {
						try {
							connections.get(address).send(message);
						} catch (DisconnectionException e) {
						}
					}
				}
			}
			handlePayload(message.getPayload());
		}
	}

	private void handlePayload(ViralPayload payload) {
		if (payload instanceof NeighborSetUpdate) {
			NeighborSetUpdate update = (NeighborSetUpdate) payload;
			Set<NodeAddress> joinedAddresses = new HashSet<>();
			Set<NodeAddress> leftAddresses = new HashSet<>();
			synchronized (model) {
				// Remember if each node was connected before the modifications
				Map<NodeAddress, Boolean> connectedBefore = new HashMap<>();
				for (NodeAddress node : model.nodeSet()) {
					connectedBefore.put(node, model.connected(localAddress, node));
				}
				// Make the modifications
				model.disconnectAll(update.getNode());
				for (NodeAddress neighbor : update.getNeighbors()) {
					model.connect(update.getNode(), neighbor);
				}
				// Check for nodes joining/leaving the network
				for (NodeAddress node : model.nodeSet()) {
					if (model.connected(localAddress, node)) {
						if (!connectedBefore.containsKey(node) || connectedBefore.get(node) == false) {
							joinedAddresses.add(node);
						}
					} else {
						if (connectedBefore.containsKey(node) && connectedBefore.get(node) == true) {
							leftAddresses.add(node);
						}
					}
				}
			}
			Set<Node> joinedNodes;
			Set<Node> leftNodes;
			synchronized (nodes) {
				// TODO: proper cleanup for nodes that left
				for (NodeAddress joinedAddress : joinedAddresses) {
					if (!nodes.containsKey(joinedAddress)) {
						nodes.put(joinedAddress,
								new ChildNode(addressedHandler, connections, localAddress, joinedAddress));
					} else {
						System.err.println(
								joinedAddress + " detected as having joined, but already is present in nodes map.");
					}
				}
				joinedNodes = joinedAddresses.stream().map(nodes::get).filter(obj -> obj != null)
						.collect(Collectors.toSet());
				leftNodes = leftAddresses.stream().map(nodes::get).filter(obj -> obj != null)
						.collect(Collectors.toSet());
			}
			synchronized (joinListeners) {
				for (Node node : joinedNodes) {
					for (int i = joinListeners.size() - 1; i >= 0; i--) {
						joinListeners.get(i).accept(node);
					}
				}
			}
			synchronized (leaveListeners) {
				for (Node node : leftNodes) {
					for (int i = leaveListeners.size() - 1; i >= 0; i--) {
						leaveListeners.get(i).accept(node);
					}
				}
			}
		} else if (payload instanceof NeighborSetUpdateTrigger) {
			Set<NodeAddress> neighborSet;
			synchronized (connections) {
				neighborSet = new HashSet<>(connections.keySet());
			}
			transmit(new NeighborSetUpdate(localAddress, neighborSet));
		} else {
			System.err.println("Invalid viral payload: " + payload);
		}
	}

}
