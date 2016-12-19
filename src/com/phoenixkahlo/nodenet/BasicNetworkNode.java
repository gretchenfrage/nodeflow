package com.phoenixkahlo.nodenet;
import java.util.function.Consumer;

/**
 * An implementation that is coupled to a BasicNetworkConnection.
 */
public class BasicNetworkNode implements NetworkNode {

	private BasicNetworkConnection connection;
	private NodeAddress address;

	public BasicNetworkNode(BasicNetworkConnection connection, NodeAddress address) {
		
	}
	
	@Override
	public void send(Object object) {

	}

	@Override
	public Object receive() {
		
	}

	@Override
	public void setReceiver(Consumer<Object> receiver) {

	}

	@Override
	public void unsetReceiver() {

	}

	@Override
	public boolean disconnect() {
		
	}

}
