package com.phoenixkahlo.nodenet.stream;

import java.net.InetSocketAddress;
import java.util.List;

import com.phoenixkahlo.nodenet.DisconnectionException;
import com.phoenixkahlo.nodenet.ProtocolViolationException;

public interface ObjectStream {

	void rebuildDeserializer();

	void send(Object object) throws DisconnectionException;

	void sendOrdered(Object object) throws DisconnectionException;

	Object receive() throws ProtocolViolationException, DisconnectionException;

	default <E> E receive(Class<E> type) throws ProtocolViolationException, DisconnectionException {
		Object received = receive();
		if (type.isAssignableFrom(received.getClass()))
			return (E) received;
		else
			throw new ProtocolViolationException("Object is of wrong class: " + received);
	}

	void disconnect();

	void setDisconnectHandler(Runnable handler, boolean launchNewThread);
	
	default void setDisconnectHandler(Runnable handler) {
		setDisconnectHandler(handler, true);
	}

	boolean isDisconnected();

	List<Object> getUnconfirmed();

	InetSocketAddress getRemoteAddress();

}