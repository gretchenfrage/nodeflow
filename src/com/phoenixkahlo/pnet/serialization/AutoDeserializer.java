package com.phoenixkahlo.pnet.serialization;

import java.io.IOException;
import java.io.InputStream;

import com.phoenixkahlo.pnet.ProtocolViolationException;

public interface AutoDeserializer {

	void autoDeserialize(InputStream in) throws IOException, ProtocolViolationException;
	
}
