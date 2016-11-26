package com.phoenixkahlo.pnet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
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
	// Lesser neighbors means neighbors have lesser hashes than keys.
	private Map<NodeAddress, Set<NodeAddress>> greaterKeys = new HashMap<>();
	private Map<NodeAddress, Set<NodeAddress>> lesserKeys = new HashMap<>();
	private NodeAddress local;

	public NetworkModel(NodeAddress local) {
		this.local = local;
		nodes.add(local);
	}

	public void addConnection(NodeAddress node1, NodeAddress node2) {
		// O(1) time complexity
		nodes.add(node1);
		nodes.add(node2);
		
		NodeAddress greater;
		NodeAddress lesser;
		if (node1.hashCode() > node2.hashCode()) {
			greater = node1;
			lesser = node2;
		} else {
			greater = node2;
			lesser = node1;
		}
		
		if (!greaterKeys.containsKey(greater))
			greaterKeys.put(greater, new HashSet<>());
		greaterKeys.get(greater).add(lesser);
		
		if (!lesserKeys.containsKey(lesser))
			lesserKeys.put(lesser, new HashSet<>());
		lesserKeys.get(lesser).add(greater);
	}

	public void removeConnection(NodeAddress node1, NodeAddress node2) {
		// O(1) time complexity
		nodes.remove(node1);
		nodes.remove(node2);
		
		NodeAddress greater;
		NodeAddress lesser;
		if (node1.hashCode() > node2.hashCode()) {
			greater = node1;
			lesser = node2;
		} else {
			greater = node2;
			lesser = node1;
		}
		
		if (greaterKeys.containsKey(greater))
			greaterKeys.get(greater).remove(lesser);
		if (lesserKeys.containsKey(lesser))
			lesserKeys.get(lesser).remove(greater);
	}

	public Set<NodeAddress> getNodes() {
		return nodes;
	}

	public Set<UnorderedTuple<NodeAddress>> getConnections() {
		// O(n) time complexity
		Set<UnorderedTuple<NodeAddress>> connections = new HashSet<>();
		for (Map.Entry<NodeAddress, Set<NodeAddress>> entry : greaterKeys.entrySet()) {
			for (NodeAddress neighbor : entry.getValue()) {
				connections.add(new UnorderedTuple<>(entry.getKey(), neighbor));
			}
		}
		return connections;
	}

	public Iterator<NodeAddress> getNeighbors(NodeAddress node) {
		// O(1) time complexity
		return new Iterator<NodeAddress>() {

			private Iterator<NodeAddress> iter1 = greaterKeys.get(node).iterator();
			private Iterator<NodeAddress> iter2 = lesserKeys.get(node).iterator();
			
			@Override
			public boolean hasNext() {
				return iter1.hasNext() || iter2.hasNext();
			}

			@Override
			public NodeAddress next() {
				if (iter1.hasNext())
					return iter1.next();
				else if (iter2.hasNext())
					return iter2.next();
				else
					throw new NoSuchElementException();
			}
			
		};
	}

	public int getShortestDistance(NodeAddress node1, NodeAddress node2) {
		// Breadth-first-search algorithm.
		// Worst case time complexity: O(V + E)
		
		Map<NodeAddress, Integer> distances = new HashMap<>();
		
		Queue<NodeAddress> queue = new LinkedList<>();
		
		distances.put(node1, 0);
		queue.add(node1);
		
		while (!queue.isEmpty()) {
			NodeAddress current = queue.remove();
			Iterable<NodeAddress> neighbors = () -> getNeighbors(current);
			for (NodeAddress neighbor : neighbors) {
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
			Iterable<NodeAddress> neighbors = () -> getNeighbors(stack.pop());
			for (NodeAddress node : neighbors) {
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
