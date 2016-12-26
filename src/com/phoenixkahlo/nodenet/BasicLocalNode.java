package com.phoenixkahlo.nodenet;

import java.net.SocketAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.phoenixkahlo.nodenet.serialization.Serializer;
import com.phoenixkahlo.nodenet.serialization.UnionSerializer;
import com.phoenixkahlo.nodenet.stream.BasicStreamFamily;
import com.phoenixkahlo.nodenet.stream.DatagramStream;
import com.phoenixkahlo.nodenet.stream.ObjectStream;
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

	private ViralMessageHandler viralHandler = new ViralMessageHandler(localAddress, connections, model);
	private AddressedMessageHandler addressedHandler = new AddressedMessageHandler(localAddress, model, connections,
			nodes);
	private HandshakeHandler handshakeHandler = new HandshakeHandler(serializer, localAddress, model, connections,
			nodes, viralHandler, addressedHandler);

	private BasicLocalNode(StreamFamily family) {
		SerializerInitializer.init(serializer);
		this.family = family;
		family.setReceiveHandler(handshakeHandler::setup);
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
		return handshakeHandler.setup(connection.get());
	}

	@Override
	public void setGreeter(Predicate<SocketAddress> test) {
		family.setReceiveTest(potential -> test.test(potential.getAddress()));
	}

	@Override
	public void listenForJoin(Consumer<Node> listener) {
		handshakeHandler.addJoinListener(listener);
	}

	@Override
	public void listenForLeave(Consumer<Node> listener) {
		handshakeHandler.addLeaveListener(listener);
	}
	
	@Override
	public void removeJoinListener(Consumer<Node> listener) {
		handshakeHandler.removeJoinListener(listener);
	}
	
	@Override
	public void removeLeaveListener(Consumer<Node> listener) {
		handshakeHandler.removeLeaveListener(listener);
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
