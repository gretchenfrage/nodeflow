package com.phoenixkahlo.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class BlockingHashMap<A, B> extends HashMap<A, B> implements BlockingMap<A, B> {
	
	private static final long serialVersionUID = -7878435603022920308L;

	@Override
	public synchronized B put(A key, B value) {
		B retrn = super.put(key, value);
		notifyAll();
		return retrn;
	}

	@Override
	public synchronized B get(Object key) {
		while (!containsKey(key)) {
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
		return super.get(key);
	}

	@Override
	public synchronized Optional<B> tryGet(A key) {
		if (containsKey(key))
			return Optional.of(super.get(key));
		else
			return Optional.empty();
	}

	@Override
	public synchronized Optional<B> tryGet(A key, long timeout) {
		Thread waitingThread = Thread.currentThread();
		Thread interruptingThread = new Thread(() -> {
			try {
				Thread.sleep(timeout);
				waitingThread.interrupt();
			} catch (InterruptedException e) {
			}
		});
		interruptingThread.start();
		long waitUntil = System.currentTimeMillis() + timeout;
		while (System.currentTimeMillis() < waitUntil && !containsKey(key)) {
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
		interruptingThread.interrupt();
		return tryGet(key);
	}

	@Override
	public synchronized void clear() {
		super.clear();
	}

	@Override
	public synchronized Object clone() {
		return super.clone();
	}

	@Override
	public synchronized B compute(A arg0, BiFunction<? super A, ? super B, ? extends B> arg1) {
		return super.compute(arg0, arg1);
	}

	@Override
	public synchronized B computeIfAbsent(A arg0, Function<? super A, ? extends B> arg1) {
		return super.computeIfAbsent(arg0, arg1);
	}

	@Override
	public synchronized B computeIfPresent(A arg0, BiFunction<? super A, ? super B, ? extends B> arg1) {
		return super.computeIfPresent(arg0, arg1);
	}

	@Override
	public synchronized boolean containsKey(Object arg0) {
		return super.containsKey(arg0);
	}

	@Override
	public synchronized boolean containsValue(Object arg0) {
		return super.containsValue(arg0);
	}

	@Override
	public synchronized Set<java.util.Map.Entry<A, B>> entrySet() {
		return super.entrySet();
	}

	@Override
	public synchronized void forEach(BiConsumer<? super A, ? super B> arg0) {
		super.forEach(arg0);
	}

	@Override
	public synchronized B getOrDefault(Object arg0, B arg1) {
		return super.getOrDefault(arg0, arg1);
	}

	@Override
	public synchronized boolean isEmpty() {
		return super.isEmpty();
	}

	@Override
	public synchronized Set<A> keySet() {
		return super.keySet();
	}

	@Override
	public synchronized B merge(A arg0, B arg1, BiFunction<? super B, ? super B, ? extends B> arg2) {
		return super.merge(arg0, arg1, arg2);
	}

	@Override
	public synchronized void putAll(Map<? extends A, ? extends B> arg0) {
		super.putAll(arg0);
	}

	@Override
	public synchronized B putIfAbsent(A arg0, B arg1) {
		return super.putIfAbsent(arg0, arg1);
	}

	@Override
	public synchronized boolean remove(Object arg0, Object arg1) {
		return super.remove(arg0, arg1);
	}

	@Override
	public synchronized B remove(Object arg0) {
		return super.remove(arg0);
	}

	@Override
	public synchronized boolean replace(A arg0, B arg1, B arg2) {
		return super.replace(arg0, arg1, arg2);
	}

	@Override
	public synchronized B replace(A arg0, B arg1) {
		return super.replace(arg0, arg1);
	}

	@Override
	public synchronized void replaceAll(BiFunction<? super A, ? super B, ? extends B> arg0) {
		super.replaceAll(arg0);
	}

	@Override
	public synchronized int size() {
		return super.size();
	}

	@Override
	public synchronized Collection<B> values() {
		return super.values();
	}

	@Override
	public synchronized boolean equals(Object arg0) {
		return super.equals(arg0);
	}

	@Override
	public synchronized int hashCode() {
		return super.hashCode();
	}

	@Override
	public synchronized String toString() {
		return super.toString();
	}
	
	

}
