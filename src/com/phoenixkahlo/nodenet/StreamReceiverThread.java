package com.phoenixkahlo.nodenet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.phoenixkahlo.nodenet.stream.DisconnectionException;
import com.phoenixkahlo.nodenet.stream.ObjectStream;

/**
 * Thread that will read messages from an ObjectStream until that stream is
 * closed and delegate them to an AddressedMessageHandler/ViralMessageHandler.
 */
public class StreamReceiverThread extends Thread {

	private static List<ObjectStream> receiving = Collections.synchronizedList(new ArrayList<>());
	
	private ObjectStream stream;
	private NodeAddress address;
	private AddressedMessageHandler addressedHandler;
	private ViralMessageHandler viralHandler;

	public StreamReceiverThread(ObjectStream stream, NodeAddress address, AddressedMessageHandler addressedHandler,
			ViralMessageHandler viralHandler) {
		if (receiving.contains(stream))
			System.err.println("Warning: multiple threads receiving from " + stream);
		receiving.add(stream);
		
		this.stream = stream;
		this.address = address;
		this.addressedHandler = addressedHandler;
		this.viralHandler = viralHandler;
	}

	@Override
	public void run() {
		try {
			while (true) {
				try {
					Object message = stream.receive();
					if (message instanceof ViralMessage)
						viralHandler.handle((ViralMessage) message);
					else if (message instanceof AddressedMessage)
						addressedHandler.handle((AddressedMessage) message, address);
					else if (message instanceof AddressedMessageResult)
						addressedHandler.handle((AddressedMessageResult) message);
					else
						throw new ProtocolViolationException("Invalid message type: " + message);
				} catch (ProtocolViolationException e) {
					System.err.println("ProtocolViolationException receiving from " + address);
					e.printStackTrace();
				}
			}
		} catch (DisconnectionException e) {
		}
		receiving.remove(stream);
	}

}
