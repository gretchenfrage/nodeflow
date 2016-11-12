package com.phoenixkahlo.pnet.socketold;

import java.io.IOException;
@Deprecated
public interface PNetSocket {

	void send(byte[] data) throws IOException;
	
	void sendOrdered(byte[] data) throws IOException;
	
	byte[] receive();
	
	void disconnect();
	
}
