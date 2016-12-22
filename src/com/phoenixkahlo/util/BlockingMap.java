package com.phoenixkahlo.util;

import java.util.Map;
import java.util.Optional;

public interface BlockingMap<A, B> extends Map<A, B> {

	Optional<B> tryGet(A key);
	
	Optional<B> tryGet(A key, long timeout);
	
}
