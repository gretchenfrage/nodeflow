package com.phoenixkahlo.pnet;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
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

	
	//SY NCNCHRONIZE EVERYTHING
	private Random random = new Random();

	private UnionSerializer serializer;

	private NodeAddress localAddress;
	private SocketFamily socketFamily;
	private NetworkModel model;
	private Map<NodeAddress, PNetObjectSocket> neighborConnections = new HashMap<>();
	private Map<NodeAddress, BasicNetworkNode> nodeReifications = new HashMap<>();
	private List<Consumer<NetworkNode>> newConnectionListeners = new ArrayList<>();

	private BasicNetworkConnection(SocketFamily socketFamily) {
		serializer = new UnionSerializer();
		SerializerInitializer.init(serializer);

		this.localAddress = new NodeAddress(random.nextInt());
		this.socketFamily = socketFamily;

		socketFamily.setReceiveHandler(this::receiveSocketConnection);
	}

	public BasicNetworkConnection(int port) throws SocketException {
		this(new BasicSocketFamily(port));
	}

	public BasicNetworkConnection() throws SocketException {
		this(new BasicSocketFamily());
	}

	@Override
	public void addSerializer(Serializer serializer, int header) {
		if (header < 0)
			throw new IllegalArgumentException("Illegal header " + header + ", negative headers are reserved");
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
		
		// Attempt to setup connection
		return setupConnection(socket.get());
	}

	/**
	 * Invoked by the underlying SocketFamily.
	 */
	private void receiveSocketConnection(PNetSocket socket) {
		setupConnection(socket);
	}

	/**
	 * Attempt to send handshake, receive handshake, update internal data
	 * structures, virally inform all nodes of new connection, and return the
	 * associated node.
	 */
	private Optional<NetworkNode> setupConnection(PNetSocket socket) {
		// Turn byte[] stream to Object stream.
		PNetObjectSocket objectSocket = new PNetObjectSocket(socket, serializer);
		
		// Create and send our own handshake.
		Handshake sendHandshake = new Handshake(localAddress, new HashSet<>(model.getConnections()));
		try {
			objectSocket.send(sendHandshake);
		} catch (IOException e) {
			System.err.println("IOException while attempting to connect " + this);
			socket.disconnect();
			return Optional.empty();
		}
		
		// Attempt to receive handshake
		Object received;
		try {
			received = objectSocket.receive();
		} catch (ProtocolViolationException e) {
			System.err.println(
					"ProtocolVioationException while attempting to connect " + this);
			socket.disconnect();
			return Optional.empty();
		}
		if (!(received instanceof Handshake)) {
			System.err.println("Handshake received is actually " + received + " at " + this);
			socket.disconnect();
			return Optional.empty();
		}
		Handshake receivedHandshake = (Handshake) received;
		
		// Update internal data structures with info of new connection
		NodeAddress connectedTo = receivedHandshake.getSenderAddress();
		model.addConnection(localAddress, connectedTo);
		BasicNetworkNode node;
		if (nodeReifications.containsKey(connectedTo)) {
			node = nodeReifications.get(connectedTo);
		} else {
			node = new BasicNetworkNode(this, connectedTo);
			nodeReifications.put(connectedTo, node);
		}
		
		// Virally inform all nodes of new connection
		// TODO: implement
		
		// Return the created node
		return Optional.of(node);
	}

	@Override
	public void setIncomingTest(Predicate<PotentialConnection> test) {
		socketFamily.setReceiveTest(potential -> test.test(new PotentialConnection(potential.getAddress())));
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
