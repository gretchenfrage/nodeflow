package com.phoenixkahlo.util;

/**
 * We need more type parameters... tune in next git commit for a QuadFunction.
 */
@FunctionalInterface
public interface TriFunction<T, U, E, R> {

	R apply(T t, U u, E e);
	
}
