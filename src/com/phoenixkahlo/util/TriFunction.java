package com.phoenixkahlo.util;

public interface TriFunction<T, U, E, R> {

	R apply(T t, U u, E e);
	
}
