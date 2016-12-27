package com.phoenixkahlo.nodenet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
	private List<Thread> receiving = Collections.synchronizedList(new ArrayList<>());
	
	private volatile boolean disconnected = false;

	public ChildNode(AddressedMessageHandler addressedHandler, Map<NodeAddress, ObjectStream> connections,
			NodeAddress localAddress, NodeAddress remoteAddress) {
		this.addressedHandler = addressedHandler;
		this.connections = connections;
		this.localAddress = localAddress;
		this.remoteAddress = remoteAddress;
	}

	@Override
	public Object receive() throws DisconnectionException {
		if (disconnected)
			throw new DisconnectionException();
		try {
			receiving.add(Thread.currentThread());
			return receivedQueue.take();
		} catch (InterruptedException e) {
			if (disconnected)
				throw new DisconnectionException();
			else
				throw new RuntimeException(e);
		} finally {
			receiving.remove(Thread.currentThread());
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
	public void send(Object object) throws DisconnectionException {
		if (disconnected)
			throw new DisconnectionException();
		addressedHandler.handle(new AddressedMessage(new ClientTransmission(object), localAddress, remoteAddress),
				localAddress);
	}

	@Override
	public boolean unlink() {
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
	public boolean isDisconnected() {
		return disconnected;
	}

	public void disconnect() {
		disconnected = true;
		receiving.forEach(Thread::interrupt);
	}

	@Override
	public String toString() {
		return "node " + remoteAddress;
	}

}
