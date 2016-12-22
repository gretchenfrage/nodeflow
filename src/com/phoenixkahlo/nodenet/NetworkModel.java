package com.phoenixkahlo.nodenet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

public class NetworkModel {

	private Map<NodeAddress, Set<NodeAddress>> connections = new HashMap<>();
	private Map<NodeAddress, Map<NodeAddress, Integer>> distances = new HashMap<>();
	private Map<NodeAddress, Map<NodeAddress, NodeAddress>> parents = new HashMap<>();
	
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
