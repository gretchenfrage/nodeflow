package com.phoenixkahlo.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An unordered, duplicate-allowing collection of items that are added with a
 * time-to-live, and will automatically be considered not a part of the
 * collection when that time expires. Has 0 thread overhead, and is not thread safe.
 */
public class TTLBag<E> implements Iterable<E> {

	private class Element {

		E item;
		long expires;

		Element(E item, long expires) {
			this.item = item;
			this.expires = expires;
		}

	}

	private List<Element> contents = new ArrayList<>();

	public void add(E item, long timeToLive) {
		contents.add(new Element(item, System.nanoTime() + timeToLive * 1_000_000));
	}

	@Override
	public Iterator<E> iterator() {
		long time = System.nanoTime();
		contents.removeIf(element -> element.expires > time);
		return contents.stream().map(element -> element.item).iterator();
	}

}
