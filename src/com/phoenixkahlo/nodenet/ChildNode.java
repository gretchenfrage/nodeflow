package com.phoenixkahlo.nodenet;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import com.phoenixkahlo.nodenet.stream.ObjectStream;

/**
 * A node that is a child of a LocalNode.
 */
public class ChildNode implements Node {

	private AddressedMessageHandler addressedHandler;
	private Map<NodeAddress, ObjectStream> connections;
	private NodeAddress localAddress;
	private NodeAddress remoteAddress;
	private BlockingQueue<Object> receivedQueue = new LinkedBlockingQueue<>();
	private Consumer<Object> receiver = receivedQueue::add;

	public ChildNode(AddressedMessageHandler addressedHandler, Map<NodeAddress, ObjectStream> connections,
			NodeAddress localAddress, NodeAddress remoteAddress) {
		this.addressedHandler = addressedHandler;
		this.connections = connections;
		this.localAddress = localAddress;
		this.remoteAddress = remoteAddress;
	}

	@Override
	public Object receive() {
		try {
			return receivedQueue.take();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setReceiver(Consumer<Object> receiver) {
		this.receiver = receiver;
	}

	@Override
	public void resetReceiver() {
		this.receiver = receivedQueue::add;
	}

	@Override
	public void send(Object object) {
		addressedHandler.handle(new AddressedMessage(new ClientTransmission(object), localAddress, remoteAddress),
				localAddress);
	}

	@Override
	public boolean disconect() {
		ObjectStream stream;
		synchronized (connections) {
			stream = connections.get(remoteAddress);
		}
		if (stream != null && !stream.isDisconnected()) {
			stream.disconnect();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Receive a message from a remote client.
	 */
	public void receiveFromParent(Object object) {
		receiver.accept(object);
	}

	@Override
	public NodeAddress getAddress() {
		return remoteAddress;
	}
	
	@Override
	public String toString() {
		return "node " + remoteAddress;
	}

}
