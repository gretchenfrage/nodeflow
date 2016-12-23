package com.phoenixkahlo.nodenet;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.phoenixkahlo.nodenet.stream.DisconnectionException;
import com.phoenixkahlo.nodenet.stream.ObjectStream;

/**
 * An object owned by a LocalNode to receive messages from neighbors and
 * delegate them to the appropriate handlers.
 */
public class NeighborReceiver extends Thread {

	private AddressedMessageHandler addressedHandler;
	private ViralMessageHandler viralHandler;
	private Map<NodeAddress, Thread> workers = Collections.synchronizedMap(new HashMap<>());

	public void setAddressedHandler(AddressedMessageHandler handler) {
		addressedHandler = handler;
	}

	public void setViralHandler(ViralMessageHandler handler) {
		viralHandler = handler;
	}
	
	public void addStream(NodeAddress node, ObjectStream stream) {
		Thread thread = new Thread(() -> {
			try {
				while (workers.contains ) {
					Object message = stream.receive();
					receive(message, node);
				}
			} catch (ProtocolViolationException | DisconnectionException e) {
			}
		});
	}
	
	public void removeStream(ObjectStream stream) {
		
	}
	
	private void receive(Object message, NodeAddress from) {
		
	}

	@Override
	public void run() {
		// TODO
	}

	public void close() {
		// TODO
	}

}
