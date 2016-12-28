package com.phoenixkahlo.nodenet;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.phoenixkahlo.nodenet.stream.ObjectStream;
import com.phoenixkahlo.util.TTLBag;

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
	private LeaveJoinHandler leaveJoinHandler;
	private TTLBag<ViralMessage> freshMessages = new TTLBag<>();
	private PrintStream errorLog;

	public ViralMessageHandler(NodeAddress localAddress, Map<NodeAddress, ObjectStream> connections,
			LeaveJoinHandler leaveJoinHandler, PrintStream errorLog) {
		this.localAddress = localAddress;
		this.connections = connections;
		this.leaveJoinHandler = leaveJoinHandler;
		this.errorLog = errorLog;
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
			synchronized (freshMessages) {
				freshMessages.add(message, 100);
			}
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

	/**
	 * Send all the fresh messages across the given connection.
	 */
	public void sendFresh(ObjectStream connection) {
		synchronized (freshMessages) {
			try {
				for (ViralMessage message : freshMessages) {
					connection.send(message);
				}
			} catch (DisconnectionException e) {
				errorLog.println("Fresh-sending failed - stream disconnected");
			}
		}
	}

	private void handlePayload(ViralPayload payload) {
		if (payload instanceof NeighborSetUpdate) {
			NeighborSetUpdate update = (NeighborSetUpdate) payload;
			leaveJoinHandler.modifyModel(model -> {
				model.disconnectAll(update.getNode());
				for (NodeAddress neighbor : update.getNeighbors()) {
					model.connect(update.getNode(), neighbor);
				}
			});
		} else if (payload instanceof NeighborSetUpdateTrigger) {
			Set<NodeAddress> neighborSet;
			synchronized (connections) {
				neighborSet = new HashSet<>(connections.keySet());
			}
			transmit(new NeighborSetUpdate(localAddress, neighborSet));
		} else {
			errorLog.println("Invalid viral payload: " + payload);
		}
	}

}
