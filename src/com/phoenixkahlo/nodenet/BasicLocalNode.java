package com.phoenixkahlo.nodenet;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.phoenixkahlo.nodenet.serialization.NullableSerializer;
import com.phoenixkahlo.nodenet.serialization.Serializer;
import com.phoenixkahlo.nodenet.serialization.UnionSerializer;
import com.phoenixkahlo.nodenet.stream.DatagramStream;
import com.phoenixkahlo.nodenet.stream.ObjectStream;
import com.phoenixkahlo.nodenet.stream.StreamFamily;

public class BasicLocalNode implements LocalNode {

	private UnionSerializer serializer = new UnionSerializer();
	private NodeAddress localAddress = new NodeAddress(ThreadLocalRandom.current().nextInt());

	private NetworkModel model;
	private StreamFamily family;
	private Map<NodeAddress, ObjectStream> connections = new HashMap<>();
	private Map<NodeAddress, Node> nodes = new HashMap<>();

	private List<Consumer<Node>> joinListeners = new ArrayList<>();
	private List<Consumer<Node>> leaveListeners = new ArrayList<>();

	private ViralMessageHandler viralHandler = new ViralMessageHandler();
	private AddressedMessageHandler addressedHandler = new AddressedMessageHandler();

	private BasicLocalNode(StreamFamily family) {
		SerializerInitializer.init(serializer);
		this.family = family;
		viralHandler.start();
		addressedHandler.start();
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
		
		ObjectStream stream = new ObjectStream(connection.get(), new NullableSerializer(serializer));
		
		stream.send(new Handshake(localAddress));
		
		Handshake received = stream.receive(Handshake.class);
		
		NodeAddress remoteAddress = received.getSenderAddress();
		
		
		
		synchronized (model) {
			model.connect(localAddress, remoteAddress);
		}
		
		synchronized (connections) {
			connections.put(remoteAddress, stream);
		}
		
		
		
	}

	@Override
	public void setGreeter(Predicate<SocketAddress> test) {
		// TODO Auto-generated method stub

	}

	@Override
	public void listenForJoin(Consumer<Node> listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void listenForLeave(Consumer<Node> listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Node> getNodes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Node> getAdjacent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub

	}

}
