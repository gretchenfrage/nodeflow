package com.phoenixkahlo.nodenet.stream;

import java.util.List;

import com.phoenixkahlo.nodenet.ProtocolViolationException;

public interface ObjectStream {

	void rebuildDeserializer();

	void send(Object object) throws DisconnectionException;

	void sendOrdered(Object object) throws DisconnectionException;

	Object receive() throws ProtocolViolationException, DisconnectionException;

	<E> E receive(Class<E> type) throws ProtocolViolationException, DisconnectionException;

	void disconnect();

	void setDisconnectHandler(Runnable handler, boolean launchNewThread);
	
	default void setDisconnectHandler(Runnable handler) {
		setDisconnectHandler(handler, true);
	}

	boolean isDisconnected();

	List<Object> getUnconfirmed();

}