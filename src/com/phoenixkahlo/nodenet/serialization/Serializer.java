package com.phoenixkahlo.nodenet.serialization;
import java.io.IOException;
import java.io.OutputStream;

/**
 * An object that can serialize a certain group of objects, and can produce a
 * symmetrical Deserializer.
 */
public interface Serializer {

	/**
	 * @return whether this can serialize object without
	 *         IllegalArgumentException.
	 */
	boolean canSerialize(Object object);

	/**
	 * Attempt to serialize object to out.
	 */
	void serialize(Object object, OutputStream out) throws IOException;

	/**
	 * @return the symmetrical deserializer.
	 */
	Deserializer toDeserializer();

}
