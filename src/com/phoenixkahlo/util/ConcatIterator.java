package com.phoenixkahlo.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;

public class ConcatIterator<E> implements Iterator<E> {

	private Stack<Iterator<E>> iterators = new Stack<>();
	
	@SafeVarargs
	public ConcatIterator(Iterator<E>... iterators) {
		for (int i = iterators.length - 1; i >= 0; i--) {
			this.iterators.push(iterators[i]);
		}
	}
	
	@Override
	public boolean hasNext() {
		while (!iterators.isEmpty() && !iterators.peek().hasNext())
			iterators.pop();
		return !iterators.isEmpty();
	}

	@Override
	public E next() {
		if (hasNext())
			return iterators.peek().next();
		else
			throw new NoSuchElementException();
	}
	
}
