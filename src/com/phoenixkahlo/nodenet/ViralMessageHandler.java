package com.phoenixkahlo.nodenet;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.phoenixkahlo.nodenet.stream.DisconnectionException;
import com.phoenixkahlo.nodenet.stream.ObjectStream;

/**
 * An object owned by a LocalNode to handle the ViralMessage system.
 */
public class ViralMessageHandler {

	private NodeAddress localAddress;
	private Set<Integer> handled = new HashSet<>();
	private Map<NodeAddress, ObjectStream> neighbors;
	private NetworkModel model;

	public ViralMessageHandler(NodeAddress localAddress, Map<NodeAddress, ObjectStream> neighbors, NetworkModel model) {
		this.localAddress = localAddress;
		this.neighbors = neighbors;
		this.model = model;
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
			synchronized (neighbors) {
				for (NodeAddress address : neighbors.keySet()) {
					if (!message.getInfected().contains(address)) {
						try {
							neighbors.get(address).send(message);
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
			synchronized (model) {
				model.disconnectAll(update.getNode());
				for (NodeAddress neighbor : update.getNeighbors()) {
					model.connect(update.getNode(), neighbor);
				}
			}
		} else if (payload instanceof NeighborSetUpdateTrigger) {
			Set<NodeAddress> neighborSet;
			synchronized (neighbors) {
				neighborSet = new HashSet<>(neighbors.keySet());
			}
			transmit(new NeighborSetUpdate(localAddress, neighborSet));
		} else {
			System.err.println("Invalid viral payload: " + payload);
		}
	}
	
}
