package com.phoenixkahlo.nodenet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Queue;
import java.util.Set;

/**
 * Graph model of the network.
 */
public class NetworkModel {

	private Map<NodeAddress, Set<NodeAddress>> connections = new HashMap<>();
	private Map<NodeAddress, Map<NodeAddress, Integer>> distances = new HashMap<>();
	private Map<NodeAddress, Map<NodeAddress, NodeAddress>> parents = new HashMap<>();

	public NetworkModel() {
	}

	public NetworkModel(NetworkModel copy) {
		for (NodeAddress node1 : copy.connections.keySet()) {
			for (NodeAddress node2 : copy.connections.get(node1)) {
				connect(node1, node2);
			}
		}
	}

	public void connect(NodeAddress node1, NodeAddress node2) {
		if (!connections.containsKey(node1))
			connections.put(node1, new HashSet<>());
		connections.get(node1).add(node2);

		if (!connections.containsKey(node2))
			connections.put(node2, new HashSet<>());
		connections.get(node2).add(node1);

		distances = new HashMap<>();
		parents = new HashMap<>();
	}

	public void disconnect(NodeAddress node1, NodeAddress node2) {
		Optional.ofNullable(connections.get(node1)).ifPresent(set -> set.remove(node2));
		if (connections.get(node1).isEmpty())
			connections.remove(node1);
		Optional.ofNullable(connections.get(node2)).ifPresent(set -> set.remove(node1));
		if (connections.get(node2).isEmpty())
			connections.remove(node2);

		distances = new HashMap<>();
		parents = new HashMap<>();
	}

	public void disconnectAll(NodeAddress node) {
		while (!connections.get(node).isEmpty()) {
			disconnect(node, connections.get(node).iterator().next());
		}
	}

	public boolean connected(NodeAddress from, NodeAddress to) {
		BFS(from);
		return distances.get(from).containsKey(to);
	}

	public OptionalInt distance(NodeAddress from, NodeAddress to) {
		BFS(from);
		if (connected(from, to))
			return OptionalInt.of(distances.get(from).get(to));
		else
			return OptionalInt.empty();
	}

	public List<NodeAddress> path(NodeAddress from, NodeAddress to) {
		BFS(from);
		if (!connected(from, to))
			throw new NoSuchElementException();
		List<NodeAddress> path = new ArrayList<>();
		NodeAddress current = to;
		while (!current.equals(from)) {
			path.add(current);
			current = parents.get(from).get(current);
		}
		path.add(from);
		Collections.reverse(path);
		return path;
	}

	private void BFS(NodeAddress root) {
		if (distances.containsKey(root) && parents.containsKey(root))
			return;

		Map<NodeAddress, Integer> distances = new HashMap<>();
		Map<NodeAddress, NodeAddress> parents = new HashMap<>();

		Queue<NodeAddress> queue = new LinkedList<NodeAddress>();

		distances.put(root, 0);
		queue.add(root);

		while (!queue.isEmpty()) {
			NodeAddress current = queue.remove();
			for (NodeAddress adj : connections.get(current)) {
				if (!distances.containsKey(adj)) {
					distances.put(adj, distances.get(current) + 1);
					parents.put(adj, current);
					queue.add(adj);
				}
			}
		}

		this.distances.put(root, distances);
		this.parents.put(root, parents);
	}

}
