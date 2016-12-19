package com.phoenixkahlo.nodenet.serialization;
import java.io.IOException;
import java.io.InputStream;

import com.phoenixkahlo.nodenet.ProtocolViolationException;

/**
 * An object that can deserialize a certain group of objects in the opposite
 * manor of a symmetrical Serializer.
 */
public interface Deserializer {

	/**
	 * Attempt to deserialize a single object from in.
	 */
	Object deserialize(InputStream in) throws IOException, ProtocolViolationException;

	/**
	 * @return the symmetrical Serializer.
	 */
	Serializer toSerializer();

}
