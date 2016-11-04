package com.phoenixkahlo.ptest;

/**
 * A mockery of a particular interface that delegates method invocations to
 * MethodMockers.
 */
public interface Mockery {

	MethodMocker method(String name, Class<?>... argTypes);

}
