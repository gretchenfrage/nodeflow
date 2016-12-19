package com.phoenixkahlo.pnet;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A concurrency-based object, based on resource IDs and corresponding resource
 * Objects. Threads can wait on resource IDs, and threads can fulfill a resource
 * ID. Threads waiting on IDs will block until the resource is fulfilled, after
 * which they will return that resource.
 */
public class ResourceWaiter<E> {

	private Map<Integer, E> resources = new HashMap<>();
	
	public E get(int id) {
		synchronized (resources) {
			while (!resources.containsKey(id)) {
				try {
					resources.wait();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			return resources.get(id);
		}
	}
	
	public Optional<E> tryGet(int id) {
		synchronized (resources) {
			while (!resources.containsKey(id)) {
				try {
					resources.wait();
				} catch (InterruptedException e) {
					return Optional.empty();
				}
			}
			return Optional.of(resources.get(id));
		}
	}
	
	public Optional<E> tryGet(int id, long timeout) {
		//TODO: Optimize to avoid thread-creation.
		Thread waiter = Thread.currentThread();
		Thread interrupter = new Thread(() -> {
			try {
				Thread.sleep(timeout);
				waiter.interrupt();
			} catch (InterruptedException e) {
			}
		});
		interrupter.start();
		try {
			synchronized (resources) {
				while (!resources.containsKey(id)) {
					resources.wait();
				}
				interrupter.interrupt();
				return Optional.of(resources.get(id));
			}
		} catch (InterruptedException e) {
			interrupter.interrupt();
			return Optional.empty();
		}
	}
	
	public void put(int id, E resource) {
		synchronized (resources) {
			resources.put(id, resource);
			resources.notifyAll();
		}
	}
	
}
