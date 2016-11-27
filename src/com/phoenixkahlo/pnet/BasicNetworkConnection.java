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
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.phoenixkahlo.pnet.serialization.Serializer;
import com.phoenixkahlo.pnet.serialization.UnionSerializer;
import com.phoenixkahlo.pnet.socket.BasicSocketFamily;
import com.phoenixkahlo.pnet.socket.PNetObjectSocket;
import com.phoenixkahlo.pnet.socket.PNetSocket;
import com.phoenixkahlo.pnet.socket.SocketFamily;
import com.phoenixkahlo.util.UnorderedTuple;

public class BasicNetworkConnection implements NetworkConnection {

	private UnionSerializer serializer; // Synchronize

	private final NodeAddress localAddress;
	private final SocketFamily socketFamily;
	private NetworkModel model; // Synchronize
	private Map<NodeAddress, PNetObjectSocket> neighborConnections = new HashMap<>(); // Synchronize
	private Map<NodeAddress, BasicNetworkNode> nodeReifications = new HashMap<>(); // Synchronize
	private List<Consumer<NetworkNode>> newConnectionListeners = new ArrayList<>(); // Synchronize

	private Set<Integer> handledVirii = new HashSet<>();

	private BasicNetworkConnection(SocketFamily socketFamily) {
		serializer = new UnionSerializer();
		SerializerInitializer.init(serializer);

		this.localAddress = new NodeAddress(ThreadLocalRandom.current().nextInt());
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
		synchronized (serializer) {
			this.serializer.add(header, serializer);
			neighborConnections.values().stream().forEach(PNetObjectSocket::rebuildDeserializer);
		}
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
		Optional<NetworkNode> connection = setupConnection(socket);
		if (connection.isPresent()) {
			synchronized (newConnectionListeners) {
				newConnectionListeners.parallelStream().forEach(listener -> listener.accept(connection.get()));
			}
		}
	}

	/**
	 * Deal with the ViralPayload as described in the ViralPayload
	 * documentation.
	 */
	public void handleViralPayload(ViralPayload virus) {
		boolean shouldHandle = false;
		synchronized (handledVirii) {
			if (!handledVirii.contains(virus.getID())) {
				handledVirii.add(virus.getID());
				shouldHandle = true;
			}
		}
		if (shouldHandle) {
			virus.addInfected(localAddress);
			// Infect neighbors
			synchronized (neighborConnections) {
				neighborConnections.entrySet().parallelStream().forEach(neighbor -> {
					if (!virus.getInfected().contains(neighbor.getKey())) {
						try {
							neighbor.getValue().send(virus);
						} catch (IOException e) {
							System.err.println("IOException sending " + virus + " to " + neighbor.getKey());
							e.printStackTrace();
						}
					}
				});
			}
			// Deal with payload
			if (virus.getPayload() instanceof ConnectionNotification) {
				ConnectionNotification payload = (ConnectionNotification) virus.getPayload();
				synchronized (model) {
					model.addConnection(payload.get1(), payload.get2());
				}
			} else if (virus.getPayload() instanceof DisconnectionNotification) {
				DisconnectionNotification payload = (DisconnectionNotification) virus.getPayload();
				synchronized (model) {
					model.removeConnection(payload.get1(), payload.get2());
				}
			} else {
				System.err.println("Invalid viral payload type: " + virus.getPayload());
			}
		}
	}

	/**
	 * Deal with the AddressedPayload as described in the AddressedPayload
	 * documentation.
	 */
	public void handleAddressedPayload(NodeAddress from, AddressedPayload addressed) {
		if (addressed.getDestination().equals(localAddress)) {
			// Transmit success
			// Deal with payload
		} else {
			// TODO: do this all in a new thread, but make it closeable
			// Prepare a model of the network that is legal to transverse
			NetworkModel transversible = model.clone();
			for (NodeAddress visited : addressed.getVisited()) {
				transversible.removeNode(visited);
			}
			transversible.trim();
			transversible.removeNode(localAddress);
			// Add local address to the list of visited addresses
			addressed.addVisited(localAddress);

			NodeAddress destination = addressed.getDestination();

			// Get the sequence of addresses to attempt transmitting through
			NodeAddress[] toAttempt = neighborConnections.keySet().stream()
					.filter(node -> transversible.getShortestDistance(node, destination).isPresent())
					.sorted((node1, node2) -> transversible.getShortestDistance(node1, destination).getAsInt()
							- transversible.getShortestDistance(node2, destination).getAsInt())
					.toArray(NodeAddress[]::new);
			/// blah blah blah ablhablahlbalhbdskljhskdhfug
		}
	}

	/**
	 * Invoked by each child of the underlying SocketFamily upon PNetSocket
	 * detection of disconnection.
	 */
	private void handleDisconnect(NodeAddress address) {
		// Update internal data structures
		synchronized (model) {
			model.removeConnection(localAddress, address);
			model.trim();
		}
		synchronized (neighborConnections) {
			neighborConnections.remove(address);
		}
		synchronized (nodeReifications) {
			nodeReifications.remove(address);
		}
		// Virally inform all nodes of disconnection
		handleViralPayload(new ViralPayload(new DisconnectionNotification(localAddress, address)));
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
			System.err.println("ProtocolVioationException while attempting to connect " + this);
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
		// Update network model
		synchronized (model) {
			model.addConnection(localAddress, connectedTo);
			for (UnorderedTuple<NodeAddress> connection : receivedHandshake.getKnownConnections()) {
				model.addConnection(connection.get1(), connection.get2());
			}
			model.trim();
		}
		BasicNetworkNode node;
		// Add to node reifications
		synchronized (nodeReifications) {
			if (nodeReifications.containsKey(connectedTo)) {
				node = nodeReifications.get(connectedTo);
			} else {
				node = new BasicNetworkNode(this, connectedTo);
				nodeReifications.put(connectedTo, node);
			}
		}
		// Add to neighbor connections
		synchronized (neighborConnections) {
			neighborConnections.put(connectedTo, objectSocket);
		}
		// Setup disconnect handler
		socket.setDisconnectHandler(() -> handleDisconnect(connectedTo));

		// Virally inform all nodes of new connection
		handleViralPayload(new ViralPayload(new ConnectionNotification(localAddress, connectedTo)));

		// Return the created node
		return Optional.of(node);
	}

	@Override
	public void setIncomingTest(Predicate<PotentialConnection> test) {
		socketFamily.setReceiveTest(potential -> test.test(new PotentialConnection(potential.getAddress())));
	}

	@Override
	public List<NetworkNode> getNodes() {
		synchronized (model) {
			model.trim();
			return model.getNodes().stream().map(nodeReifications::get).collect(Collectors.toList());
		}
	}

	@Override
	public List<NetworkNode> getAdjacentNodes() {
		synchronized (neighborConnections) {
			return neighborConnections.keySet().stream().map(nodeReifications::get).collect(Collectors.toList());
		}
	}

	@Override
	public void disconnect() {
		socketFamily.close();
	}

	@Override
	public void listenForConnection(Consumer<NetworkNode> listener) {
		synchronized (newConnectionListeners) {
			newConnectionListeners.add(listener);
		}
	}

}
