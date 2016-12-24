package com.phoenixkahlo.nodenet;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import com.phoenixkahlo.nodenet.stream.ObjectStream;

/**
 * An iterator for the sequence of neighbors to attempt to send an
 * AddressedMessage through.
 */
public class AddressedAttemptSequence implements Iterator<NodeAddress> {

	private NetworkModel model;
	private AddressedMessage message;
	private NodeAddress localAddress;
	private Map<NodeAddress, ObjectStream> connections;
	private Set<NodeAddress> attempted = new HashSet<>();
	
	private Optional<NodeAddress> nextCache;

	public AddressedAttemptSequence(NetworkModel model, AddressedMessage message, NodeAddress localAddress,
			Map<NodeAddress, ObjectStream> connections) {
		this.model = model;
		this.message = message;
		this.localAddress = localAddress;
		this.connections = connections;
	}

	private Optional<NodeAddress> optionalNext() {
		// Prepare the set of neighbors that are legal to attempt
		Set<NodeAddress> possible;
		synchronized (connections) {
			possible = new HashSet<>(connections.keySet());
		}
		possible.removeIf(node -> attempted.contains(node));
		possible.removeIf(node -> message.getVisited().contains(node));
		// Prepare the model of the network that is legal to transverse
		NetworkModel legal;
		synchronized (model) {
			legal = new NetworkModel(model);
		}
		message.getVisited().forEach(legal::disconnectAll);
		legal.disconnectAll(localAddress);
		// Remove neighbors that cannot reach the destination
		possible.removeIf(node -> !legal.connected(node, message.getDestination()));
		// Prepare to compare addresses by which is closer to the destination
		Comparator<NodeAddress> closer = (node1, node2) -> {
			int dist1 = legal.distance(node1, message.getDestination()).getAsInt();
			int dist2 = legal.distance(node2, message.getDestination()).getAsInt();
			return dist2 - dist1;
		};
		// Find the closest possible neighbor
		return possible.stream().sorted(closer).findFirst();
	}

	@Override
	public boolean hasNext() {
		if (nextCache == null)
			nextCache = optionalNext();
		
		return nextCache.isPresent();
	}

	@Override
	public NodeAddress next() {
		if (nextCache == null)
			nextCache = optionalNext();
		
		Optional<NodeAddress> next = nextCache;
		nextCache = null;
		if (next.isPresent()) {
			attempted.add(next.get());
			return next.get();
		} else {
			throw new NoSuchElementException();
		}
	}

}
