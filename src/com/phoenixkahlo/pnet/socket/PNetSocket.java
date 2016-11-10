package com.phoenixkahlo.pnet.socket;

import java.io.IOException;

public interface PNetSocket {

	void send(byte[] data) throws IOException;
	
	void sendOrdered(byte[] data) throws IOException;
	
	byte[] receive();
	
	void disconnect();
	
}
