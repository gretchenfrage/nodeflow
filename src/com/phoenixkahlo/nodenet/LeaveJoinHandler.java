package com.phoenixkahlo.nodenet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * An object owned by a LocalNode to handle addresses leaving/joining the
 * network.
 */
public class LeaveJoinHandler {

	private NodeAddress localAddress;
	private List<Consumer<Node>> joinListeners = new ArrayList<>();
	private List<Consumer<Node>> leaveListeners = new ArrayList<>();
	private NetworkModel model;
	private Map<NodeAddress, ChildNode> nodes;
	private Function<NodeAddress, ChildNode> nodeFactory;

	public LeaveJoinHandler(NodeAddress localAddress, NetworkModel model, Map<NodeAddress, ChildNode> nodes,
			Function<NodeAddress, ChildNode> nodeFactory) {
		this.localAddress = localAddress;
		this.model = model;
		this.nodes = nodes;
		this.nodeFactory = nodeFactory;
	}

	/**
	 * Add a ChildNode to the nodes map, and invoke all join handlers, if
	 * doesn't already exist.
	 */
	public synchronized void handleJoin(NodeAddress address) {
		ChildNode node = null;
		synchronized (nodes) {
			if (!nodes.containsKey(address) || nodes.get(address).isDisconnected()) {
				node = nodeFactory.apply(address);
				nodes.put(address, node);
			}
		}
		if (node != null) {
			synchronized (joinListeners) {
				for (int i = joinListeners.size() - 1; i >= 0; i--) {
					joinListeners.get(i).accept(node);
				}
			}
		}
	}

	/**
	 * Remove the ChildNode from the nodes map, and invoke all leave handlers,
	 * unless doesn't exist.
	 */
	public synchronized void handleLeave(NodeAddress address) {
		ChildNode node = null;
		synchronized (nodes) {
			if (nodes.containsKey(address)) {
				node = nodes.get(address);
				node.disconnect();
				nodes.remove(address);
			}
		}
		if (node != null) {
			synchronized (leaveListeners) {
				for (int i = leaveListeners.size() - 1; i >= 0; i--) {
					leaveListeners.get(i).accept(node);
				}
			}
		}
	}

	/**
	 * Allow a mutator to change the NetworkModel, and then invoke
	 * handleJoin/handleLeave as appropriate.
	 */
	public synchronized void modifyModel(Consumer<NetworkModel> mutator) {
		Set<NodeAddress> joined = new HashSet<>();
		Set<NodeAddress> left = new HashSet<>();
		synchronized (model) {
			Map<NodeAddress, Boolean> connectedBefore = new HashMap<>();
			for (NodeAddress address : model.nodeSet()) {
				connectedBefore.put(address, model.connected(localAddress, address));
			}
			mutator.accept(model);
			for (NodeAddress node : model.nodeSet()) {
				if (model.connected(localAddress, node)) {
					if (!connectedBefore.containsKey(node) || connectedBefore.get(node) == false) {
						joined.add(node);
					}
				} else {
					if (connectedBefore.containsKey(node) && connectedBefore.get(node) == true) {
						left.add(node);
					}
				}
			}
		}
		joined.forEach(this::handleJoin);
		left.forEach(this::handleLeave);
	}

	public void listenForJoin(Consumer<Node> listener) {
		synchronized (joinListeners) {
			joinListeners.add(listener);
		}
	}

	public void listenForLeave(Consumer<Node> listener) {
		synchronized (leaveListeners) {
			leaveListeners.add(listener);
		}
	}

	public void removeJoinListener(Consumer<Node> listener) {
		synchronized (joinListeners) {
			joinListeners.remove(listener);
		}
	}

	public void removeLeaveListener(Consumer<Node> listener) {
		synchronized (leaveListeners) {
			leaveListeners.remove(listener);
		}
	}

}
