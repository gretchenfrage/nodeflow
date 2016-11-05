package com.phoenixkahlo.pnet.serialization;

import java.io.IOException;
import java.io.OutputStream;

public interface AutoSerializer {

	void autoSerialize(OutputStream out) throws IOException;
	
}
