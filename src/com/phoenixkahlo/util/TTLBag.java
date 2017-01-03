package com.phoenixkahlo.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An unordered, duplicate-allowing collection of items that are added with a
 * time-to-live, and will automatically be considered not a part of the
 * collection when that time expires.
 */
public class TTLBag<E> implements Iterable<E> {

	private List<Tuple<E, Long>> contents = new ArrayList<>();

	public void add(E item, long timeToLive) {
		contents.add(new Tuple<>(item, System.currentTimeMillis() + timeToLive));
	}

	@Override
	public Iterator<E> iterator() {
		long time = System.currentTimeMillis();
		contents.removeIf(tuple -> tuple.getB() > time);
		return contents.stream().map(Tuple::getA).iterator();
	}

}
