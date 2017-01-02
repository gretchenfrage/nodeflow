package com.phoenixkahlo.nodenet;

import java.io.PrintStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.phoenixkahlo.nodenet.stream.ObjectStream;
import com.phoenixkahlo.util.BlockingMap;
import com.phoenixkahlo.util.ConcatIterator;

/**
 * A thread owned by an AddressedMessageHandler that will try to get an
 * AddressedMessage to its destination, and then send the result of if it has
 * failed or succeeded.
 */
public class AddressedDelegatorThread extends Thread {

	private AddressedMessage message;
	private NodeAddress localAddress;
	private NetworkModel model;
	private Map<NodeAddress, ObjectStream> connections;
	private BlockingMap<Integer, Boolean> addressedResults;
	private NodeAddress sender;

	private Object sleepLock = new Object();

	private volatile boolean done = false;
	private volatile boolean succeeded = false;

	private volatile boolean sequenceComplete = false;
	private Set<NodeAddress> sequenceAccumulation = Collections.synchronizedSet(new HashSet<>());

	private PrintStream errorLog;

	public AddressedDelegatorThread(AddressedMessage message, NodeAddress localAddress, NetworkModel model,
			Map<NodeAddress, ObjectStream> connections, BlockingMap<Integer, Boolean> addressedResults,
			NodeAddress sender, PrintStream errorLog) {
		this.message = message;
		this.localAddress = localAddress;
		this.model = model;
		this.connections = connections;
		this.addressedResults = addressedResults;
		this.sender = sender;
		this.errorLog = errorLog;
	}

	private void receiveResult(NodeAddress from, boolean wasSuccessful) {
		synchronized (sleepLock) {
			if (wasSuccessful) {
				this.succeeded = true;
				this.done = true;
				sleepLock.notifyAll();
			} else if (sequenceComplete) {
				boolean allFailed;
				synchronized (addressedResults) {
					allFailed = sequenceAccumulation.stream().allMatch(
							node -> addressedResults.containsKey(node) && addressedResults.get(node) == false);
				}
				if (allFailed) {
					this.done = true;
					sleepLock.notifyAll();
				}
			}
		}
	}

	@Override
	public void run() {
		synchronized (sleepLock) {
			Iterator<NodeAddress> sequence = new ConcatIterator<>(
					new AddressedAttemptSequence(model, message, localAddress, connections),
					new AddressedAttemptSequence(model, message, localAddress, connections));
			while (sequence.hasNext() && !done) {
				NodeAddress next = sequence.next();
				sequenceAccumulation.add(next);

				message.randomizeTransmissionID();
				int transmissionID = message.getTransmissionID();

				ObjectStream stream;
				synchronized (connections) {
					stream = connections.get(next);
				}
				if (stream == null) {
					errorLog.println("Failed to send AddressedMessage to " + sender + " - stream not found");
				}
				try {
					stream.send(message);
				} catch (DisconnectionException e) {
					errorLog.println("Failed to send AddressedMessage to " + sender + " - stream disconnected");
					addressedResults.put(transmissionID, false);
				}

				Thread waiter = new Thread(() -> {
					Optional<Boolean> result = addressedResults.tryGet(transmissionID, 10_000);
					if (result.isPresent()) {
						receiveResult(next, result.get());
					}
				});
				waiter.start();

				try {
					sleepLock.wait(500);
				} catch (InterruptedException e) {
				}
			}
			sequenceComplete = true;

			if (!localAddress.equals(sender)) {
				ObjectStream stream;
				synchronized (connections) {
					stream = connections.get(sender);
				}
				if (stream == null) {
					errorLog.println("Failed to send result to " + sender + " - stream not found");
					return;
				}
				try {
					stream.send(new AddressedMessageResult(message.getOriginalTransmissionID(), succeeded));
				} catch (DisconnectionException e) {
					errorLog.println("Failed to send result to " + sender + " - stream disconnected");
				}
			}
		}
	}

}
