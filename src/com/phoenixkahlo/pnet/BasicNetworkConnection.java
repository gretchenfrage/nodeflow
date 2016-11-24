package com.phoenixkahlo.pnet;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.phoenixkahlo.pnet.serialization.Serializer;
import com.phoenixkahlo.pnet.serialization.UnionSerializer;
import com.phoenixkahlo.pnet.socket.BasicSocketFamily;
import com.phoenixkahlo.pnet.socket.PNetObjectSocket;
import com.phoenixkahlo.pnet.socket.PNetSocket;
import com.phoenixkahlo.pnet.socket.SocketFamily;

public class BasicNetworkConnection implements NetworkConnection {

	private Random random = new Random();

	private UnionSerializer serializer;

	private NodeAddress localAddress;
	private SocketFamily socketFamily;
	private NetworkModel model;
	private Map<NodeAddress, PNetObjectSocket> neighborConnections = new HashMap<>();
	private Map<NodeAddress, BasicNetworkNode> nodeReifications = new HashMap<>();

	private BasicNetworkConnection(SocketFamily socketFamily) {
		serializer = new UnionSerializer();
		SerializerInitializer.init(serializer);

		this.localAddress = new NodeAddress(random.nextInt());
		this.socketFamily = socketFamily;
	}

	public BasicNetworkConnection(int port) throws SocketException {
		this(new BasicSocketFamily(port));
	}

	public BasicNetworkConnection() throws SocketException {
		this(new BasicSocketFamily());
	}

	@Override
	public void addSerializer(Serializer serializer, int header) {
		this.serializer.add(header, serializer);
		neighborConnections.values().parallelStream().forEach(PNetObjectSocket::rebuildDeserializer);
	}

	@Override
	public Optional<NetworkNode> connect(SocketAddress address) {
		// Attempt to form the PNetSocket
		Optional<PNetSocket> socket = socketFamily.connect(address);

		// Fail if PNetSocket form failed.
		if (!socket.isPresent())
			return Optional.empty();

		// Turn byte[] stream to Object stream.
		PNetObjectSocket objectSocket = new PNetObjectSocket(socket.get(), serializer);

		// Create and send our own handshake.
		Handshake sendHandshake = new Handshake(localAddress, new HashSet<>(model.getConnections()));
		try {
			objectSocket.send(sendHandshake);
		} catch (IOException e) {
			System.err.println("IOException while attempting to connect " + this + " to " + address + ": " + e);
			socket.get().disconnect();
			return Optional.empty();
		}

		// Attempt to receive handshake
		Object received;
		try {
			received = objectSocket.receive();
		} catch (ProtocolViolationException e) {
			System.err.println(
					"ProtocolVioationException while attempting to connect " + this + " to " + address + ": " + e);
			socket.get().disconnect();
			return Optional.empty();
		}
		if (!(received instanceof Handshake)) {
			System.err.println("Handshake received is actually " + received + " at " + this);
			socket.get().disconnect();
			return Optional.empty();
		}
		Handshake receivedHandshake = (Handshake) received;
		
		// Update internal data structures with info of new connection
		NodeAddress connectedTo = receivedHandshake.getSenderAddress();
		neighborConnections.put(connectedTo, objectSocket);
		nodeReifications.put(connectedTo, new BasicNetworkNode(this, connectedTo));
		model.addConnection(localAddress, connectedTo);
		
		// Virally inform all nodes of new connection
		must be implemented
	}

	@Override
	public void setIncomingTest(Predicate<PotentialConnection> test) {

	}

	@Override
	public List<NetworkNode> getNodes() {

	}

	@Override
	public void disconnect() {

	}

	@Override
	public void listenForConnection(Consumer<NetworkNode> listener) {

	}

}
