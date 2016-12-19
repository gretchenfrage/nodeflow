package com.phoenixkahlo.pnet.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.phoenixkahlo.pnet.ProtocolViolationException;

/**
 * AutoSerializers will be allowed to take control of their final stage of
 * serialization and deserialization.
 */
public interface AutoSerializer {

	void autoSerialize(OutputStream out) throws IOException;
	
	void autoDeserialize(InputStream in) throws IOException, ProtocolViolationException;
	
}
