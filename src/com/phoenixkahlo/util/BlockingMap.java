package com.phoenixkahlo.util;

import java.util.Map;
import java.util.Optional;

/**
 * A blocking map functions as a normal map, except that if the get() operation is performed with a non-existent key,
 * the thread will block until an item with that key is inserted. In addition, the blocking map adds two additional
 * methods - one that will attempt to get the item without blocking, and one that will attempt to get the item
 * with a maximum blocking time.
 */
public interface BlockingMap<A, B> extends Map<A, B> {

	Optional<B> tryGet(A key);
	
	Optional<B> tryGet(A key, long timeout);
	
}
