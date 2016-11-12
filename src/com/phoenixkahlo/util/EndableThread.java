package com.phoenixkahlo.util;

/**
 * A thread that provides a method to elegantly shut itself down.
 */
public interface EndableThread {

	void start();

	void end();

}
