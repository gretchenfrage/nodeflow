package com.phoenixkahlo.nodenet;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import com.phoenixkahlo.nodenet.serialization.Serializer;
import com.phoenixkahlo.nodenet.stream.DatagramStream;
import com.phoenixkahlo.nodenet.stream.ObjectStream;
import com.phoenixkahlo.nodenet.stream.SerializerObjectStream;

/**
 * An object owned by a LocalNode to handle the AddressedMessage system. When a
 * new connection is formed, the LocalNode sends it to the HandshakeHandler to
 * set it up and return a node if it succeeds in setup.
 */
public class HandshakeHandler {

	private Serializer serializer;
	private NodeAddress localAddress;
	private Map<NodeAddress, ObjectStream> connections;
	private Map<NodeAddress, ChildNode> nodes;
	private ViralMessageHandler viralHandler;
	private AddressedMessageHandler addressedHandler;

	private LeaveJoinHandler leaveJoinHandler;

	public HandshakeHandler(Serializer serializer, NodeAddress localAddress, Map<NodeAddress, ObjectStream> connections,
			Map<NodeAddress, ChildNode> nodes, ViralMessageHandler viralHandler,
			AddressedMessageHandler addressedHandler, LeaveJoinHandler leaveJoinHandler) {
		this.serializer = serializer;
		this.localAddress = localAddress;
		this.connections = connections;
		this.nodes = nodes;
		this.viralHandler = viralHandler;
		this.addressedHandler = addressedHandler;
		this.leaveJoinHandler = leaveJoinHandler;
	}

	public Optional<Node> setup(DatagramStream connection) {
		// Establish object transmission layer
		ObjectStream stream = new SerializerObjectStream(connection, serializer);

		// Exchange handshakes, find remote address
		Handshake received;
		try {
			stream.send(new Handshake(localAddress));
			received = stream.receive(Handshake.class);
		} catch (ProtocolViolationException | DisconnectionException e) {
			return Optional.empty();
		}
		NodeAddress remoteAddress = received.getSenderAddress();

		// Add to connections map
		synchronized (connections) {
			connections.put(remoteAddress, stream);
		}

		// Let leaveJoinHandler set up the new node
		Node node;
		synchronized (nodes) {
			leaveJoinHandler.modifyModel(model -> model.connect(localAddress, remoteAddress));
			node = nodes.get(remoteAddress);
		}

		// Setup receiver thread
		new StreamReceiverThread(stream, remoteAddress, addressedHandler, viralHandler).start();

		// Setup disconnect handler
		stream.setDisconnectHandler(() -> {
			// Update model
			leaveJoinHandler.modifyModel(model -> model.disconnect(localAddress, remoteAddress));
			// Remove from connections
			// Transmit NeighborSetUpdate
			synchronized (connections) {
				connections.remove(remoteAddress);
				viralHandler.transmit(new NeighborSetUpdate(localAddress, new HashSet<>(connections.keySet())));
			}
		});

		// Send fresh viral message to new connection
		viralHandler.sendFresh(stream);

		// Propagate neighbor set update trigger
		viralHandler.transmit(new NeighborSetUpdateTrigger());

		// Return node
		return Optional.of(node);
	}

}
