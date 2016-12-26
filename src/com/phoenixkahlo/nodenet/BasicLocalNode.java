package com.phoenixkahlo.nodenet;

import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.phoenixkahlo.nodenet.serialization.NullableSerializer;
import com.phoenixkahlo.nodenet.serialization.Serializer;
import com.phoenixkahlo.nodenet.serialization.UnionSerializer;
import com.phoenixkahlo.nodenet.stream.BasicStreamFamily;
import com.phoenixkahlo.nodenet.stream.DatagramStream;
import com.phoenixkahlo.nodenet.stream.DisconnectionException;
import com.phoenixkahlo.nodenet.stream.ObjectStream;
import com.phoenixkahlo.nodenet.stream.SerializerObjectStream;
import com.phoenixkahlo.nodenet.stream.StreamFamily;

/**
 * Implementation of LocalNode.
 */
public class BasicLocalNode implements LocalNode {

	private UnionSerializer serializer = new UnionSerializer();
	private NodeAddress localAddress = new NodeAddress(ThreadLocalRandom.current().nextInt());

	private NetworkModel model;
	private StreamFamily family;
	private Map<NodeAddress, ObjectStream> connections = new HashMap<>();
	private Map<NodeAddress, ChildNode> nodes = new HashMap<>();

	private List<Consumer<Node>> joinListeners = new ArrayList<>();
	private List<Consumer<Node>> leaveListeners = new ArrayList<>();

	private ViralMessageHandler viralHandler = new ViralMessageHandler(localAddress, connections, model);
	private AddressedMessageHandler addressedHandler = new AddressedMessageHandler(localAddress, model, connections,
			nodes);
	
	private BasicLocalNode(StreamFamily family) {
		SerializerInitializer.init(serializer);
		this.family = family;
		family.setReceiveHandler(this::setup);
	}

	public BasicLocalNode() throws SocketException {
		this(new BasicStreamFamily());
	}

	public BasicLocalNode(int port) throws SocketException {
		this(new BasicStreamFamily(port));
	}

	@Override
	public void addSerializer(Serializer serializer, int header) {
		if (header < 0)
			throw new IllegalArgumentException("Serializer headers must be positive");

		synchronized (this.serializer) {
			this.serializer.add(header, serializer);
			connections.values().stream().forEach(ObjectStream::rebuildDeserializer);
		}
	}

	@Override
	public Optional<Node> connect(SocketAddress address) {
		Optional<DatagramStream> connection = family.connect(address);
		if (!connection.isPresent())
			return Optional.empty();
		return setup(connection.get());
	}

	
	private Optional<Node> setup(DatagramStream connection) {
		ObjectStream stream = new SerializerObjectStream(connection, new NullableSerializer(serializer));
		
		Handshake received;
		try {
			stream.send(new Handshake(localAddress));
			received = stream.receive(Handshake.class);
		} catch (ProtocolViolationException | DisconnectionException e) {
			return Optional.empty();
		}
		NodeAddress remoteAddress = received.getSenderAddress();

		boolean alreadyConnected = model.connected(localAddress, remoteAddress);

		synchronized (model) {
			model.connect(localAddress, remoteAddress);
		}

		synchronized (connections) {
			connections.put(remoteAddress, stream);
		}

		ChildNode node = new ChildNode(addressedHandler, connections, localAddress, remoteAddress);
		synchronized (nodes) {
			nodes.put(remoteAddress, node);
		}

		viralHandler.transmit(new NeighborSetUpdateTrigger());

		new StreamReceiverThread(stream, remoteAddress, addressedHandler, viralHandler).start();


		
		if (!alreadyConnected) {
			synchronized (joinListeners) {
				joinListeners.forEach(listener -> listener.accept(node));
			}
		}

		stream.setDisconnectHandler(() -> {
			synchronized (model) {
				model.disconnect(localAddress, remoteAddress);
			}
			synchronized (connections) {
				connections.remove(remoteAddress);
			}
			synchronized (connections) {
				viralHandler.transmit(new NeighborSetUpdate(localAddress, new HashSet<>(connections.keySet())));
			}
			List<NodeAddress> addresses;
			synchronized (nodes) {
				addresses = nodes.keySet().stream().collect(Collectors.toList());
			}
			synchronized (model) {
				addresses.removeIf(address -> model.connected(localAddress, address));
			}
			synchronized (nodes) {
				addresses.stream().map(nodes::get)
						.forEach(left -> leaveListeners.forEach(listener -> listener.accept(left)));
			}
		});

		return Optional.of(node);
	}

	@Override
	public void setGreeter(Predicate<SocketAddress> test) {
		family.setReceiveTest(potential -> test.test(potential.getAddress()));
	}

	@Override
	public void listenForJoin(Consumer<Node> listener) {
		synchronized (joinListeners) {
			joinListeners.add(listener);
		}
	}

	@Override
	public void listenForLeave(Consumer<Node> listener) {
		synchronized (leaveListeners) {
			leaveListeners.add(listener);
		}
	}

	@Override
	public List<Node> getNodes() {
		synchronized (nodes) {
			return nodes.values().stream().collect(Collectors.toList());
		}
	}

	@Override
	public List<Node> getAdjacent() {
		List<NodeAddress> addresses;
		synchronized (connections) {
			addresses = connections.keySet().stream().collect(Collectors.toList());
		}
		synchronized (nodes) {
			return addresses.stream().map(nodes::get).collect(Collectors.toList());
		}
	}

	@Override
	public void disconnect() {
		family.close();
	}

}
