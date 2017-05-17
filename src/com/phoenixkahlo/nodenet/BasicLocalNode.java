package com.phoenixkahlo.nodenet;

import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.esotericsoftware.kryo.Kryo;
import com.phoenixkahlo.nodenet.proxy.BasicProxy;
import com.phoenixkahlo.nodenet.proxy.Proxy;
import com.phoenixkahlo.nodenet.proxy.ProxyHandler;
import com.phoenixkahlo.nodenet.proxy.ProxyInvocation;
import com.phoenixkahlo.nodenet.proxy.ProxyMultiInvocation;
import com.phoenixkahlo.nodenet.proxy.ProxyResult;
import com.phoenixkahlo.nodenet.serialization.*;
import com.phoenixkahlo.nodenet.stream.BasicStreamFamily;
import com.phoenixkahlo.nodenet.stream.DatagramStream;
import com.phoenixkahlo.nodenet.stream.ObjectStream;
import com.phoenixkahlo.nodenet.stream.StreamFamily;
import com.phoenixkahlo.util.UUID;

/**
 * Implementation of LocalNode.
 */
public class BasicLocalNode implements LocalNode {

	private Kryo kryo = new Kryo();
	private NodeAddress localAddress = new NodeAddress(new UUID());

	private NetworkModel model = new NetworkModel();
	private StreamFamily family;
	private Map<NodeAddress, ObjectStream> connections = new HashMap<>();
	private Map<NodeAddress, ChildNode> nodes = new HashMap<>();

	private AddressedMessageHandler addressedHandler;
	private Function<NodeAddress, ChildNode> nodeFactory;
	private LeaveJoinHandler leaveJoinHandler;
	private ViralMessageHandler viralHandler;
	private HandshakeHandler handshakeHandler;
	private ProxyHandler proxyHandler;

	private BasicLocalNode(StreamFamily family, PrintStream errorLog) {
		this.family = family;
		addressedHandler = new AddressedMessageHandler(localAddress, model, connections, nodes, errorLog);
		nodeFactory = address -> new ChildNode(addressedHandler, connections, localAddress, address);
		leaveJoinHandler = new LeaveJoinHandler(localAddress, model, nodes, nodeFactory);
		viralHandler = new ViralMessageHandler(localAddress, connections, leaveJoinHandler, errorLog);
		handshakeHandler = new HandshakeHandler(kryo, localAddress, connections, nodes, viralHandler,
				addressedHandler, leaveJoinHandler, errorLog);
		proxyHandler = new ProxyHandler(addressedHandler, localAddress, this::getNode);
		addressedHandler.setProxyHandler(proxyHandler);
		//initSerializer();
		family.setReceiveHandler(handshakeHandler::setup);
		nodes.put(localAddress, nodeFactory.apply(localAddress));
	}

	public BasicLocalNode(PrintStream errorLog) throws SocketException {
		this(new BasicStreamFamily(errorLog), errorLog);
	}

	public BasicLocalNode(int port, PrintStream errorLog) throws SocketException {
		this(new BasicStreamFamily(port, errorLog), errorLog);
	}

	public BasicLocalNode() throws SocketException {
		this(System.err);
	}

	public BasicLocalNode(int port) throws SocketException {
		this(port, System.err);
	}

	@Override
	public Kryo getKryo() {
		return kryo;
	}

	@Override
	public Optional<Node> connect(InetSocketAddress address) {
		Optional<NodeAddress> alreadyConnected;
		synchronized (connections) {
			alreadyConnected = connections.entrySet().stream()
					.filter(entry -> entry.getValue().getRemoteAddress().equals(address)).map(Entry::getKey).findAny();
		}
		if (alreadyConnected.isPresent()) {
			synchronized (nodes) {
				if (nodes.containsKey(alreadyConnected.get()))
					return Optional.of(nodes.get(alreadyConnected.get()));
			}
		}

		Optional<DatagramStream> connection = family.connect(address);
		if (!connection.isPresent())
			return Optional.empty();
		return handshakeHandler.setup(connection.get());
	}

	@Override
	public void setGreeter(Predicate<InetSocketAddress> test) {
		family.setReceiveTest(potential -> test.test(potential.getAddress()));
	}

	@Override
	public void listenForJoin(Consumer<Node> listener) {
		leaveJoinHandler.listenForJoin(listener);
	}

	@Override
	public void listenForLeave(Consumer<Node> listener) {
		leaveJoinHandler.listenForLeave(listener);
	}

	@Override
	public void removeJoinListener(Consumer<Node> listener) {
		leaveJoinHandler.removeJoinListener(listener);
	}

	@Override
	public void removeLeaveListener(Consumer<Node> listener) {
		leaveJoinHandler.removeLeaveListener(listener);
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

	@Override
	public NodeAddress getAddress() {
		return localAddress;
	}

	@Override
	public Optional<Node> getNode(NodeAddress address) {
		synchronized (nodes) {
			return Optional.ofNullable(nodes.get(address));
		}
	}

	@Override
	public <E> Proxy<E> makeProxy(E source, Class<E> intrface) {
		return proxyHandler.makeProxy(source, intrface);
	}

	@Override
	public void removeProxy(Object source) {
		proxyHandler.removeProxy(source);
	}

	@Override
	public void removeProxy(Proxy<?> proxy) {
		proxyHandler.removeProxy(proxy);
	}

}
