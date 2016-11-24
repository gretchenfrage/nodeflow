package com.phoenixkahlo.pnet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import com.phoenixkahlo.util.UnorderedTuple;

/**
 * A model of all the nodes and connections in the network. One node is
 * considered local, and any node not directly/indirectly connected to local is
 * trimmed from the model.
 */
public class NetworkModel {

	private Set<NodeAddress> nodes = new HashSet<>();
	private Set<UnorderedTuple<NodeAddress>> connections = new HashSet<>();
	private NodeAddress local;

	public NetworkModel(NodeAddress local) {
		this.local = local;
		nodes.add(local);
	}

	public void addConnection(NodeAddress node1, NodeAddress node2) {
		nodes.add(node1);
		nodes.add(node2);
		connections.add(new UnorderedTuple<>(node1, node2));
	}

	public void removeConnection(NodeAddress node1, NodeAddress node2) {
		nodes.remove(node1);
		nodes.remove(node2);
		connections.remove(new UnorderedTuple<>(node1, node2));
	}

	public Set<NodeAddress> getNodes() {
		return nodes;
	}

	public Set<UnorderedTuple<NodeAddress>> getConnections() {
		return connections;
	}

	public Set<NodeAddress> getNeighbors(NodeAddress node) {
		Set<NodeAddress> neighbors = new HashSet<>();
		for (UnorderedTuple<NodeAddress> connection : connections) {
			if (connection.get1().equals(node))
				neighbors.add(connection.get2());
			if (connection.get2().equals(node))
				neighbors.add(connection.get1());
		}
		return neighbors;
	}

	public int getShortestDistance(NodeAddress node1, NodeAddress node2) {
		// Breadth-first-search algorithm.

		Map<NodeAddress, Integer> distances = new HashMap<>();
		
		Queue<NodeAddress> queue = new LinkedList<>();
		
		distances.put(node1, 0);
		queue.add(node1);
		
		while (!queue.isEmpty()) {
			NodeAddress current = queue.remove();
			for (NodeAddress neighbor : getNeighbors(current)) {
				if (!distances.containsKey(neighbor)) {
					distances.put(neighbor, distances.get(current) + 1);
					queue.add(neighbor);
				}
				
				if (neighbor.equals(node2))
					return distances.get(node2);
			}
		}
		
		throw new IllegalArgumentException("BFS failure.");
	}

	public void trim() {
		// The set of nodes that have been added to the stack.
		Set<NodeAddress> touched = new HashSet<>();
		// The stack of nodes which's untouched neighbors should be recursed on.
		Stack<NodeAddress> stack = new Stack<>();

		touched.add(local);
		stack.push(local);

		while (!stack.isEmpty()) {
			for (NodeAddress node : getNeighbors(stack.pop())) {
				if (!touched.contains(node)) {
					stack.push(node);
					touched.add(node);
				}
			}
		}

		// At this point, touched should contain all nodes connected to local.

		Iterator<NodeAddress> iterator = nodes.iterator();
		while (iterator.hasNext())
			if (!touched.contains(iterator.next()))
				iterator.remove();
	}

}
