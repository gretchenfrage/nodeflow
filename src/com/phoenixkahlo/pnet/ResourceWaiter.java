package com.phoenixkahlo.pnet;

import java.util.HashMap;
import java.util.Map;

/**
 * A concurrency-based object, based on resource IDs and corresponding resource
 * Objects. Threads can wait on resource IDs, and threads can fulfill a resource
 * ID. Threads waiting on IDs will block until the resource is fulfilled, after
 * which they will return that resource.
 */
public class ResourceWaiter {

	private Map<Integer, Object> resources = new HashMap<>();
	
	public Object waitForResource(int id) throws InterruptedException {
		synchronized (resources) {
			while (!resources.containsKey(id)) {
				resources.wait();
			}
			return resources.get(id);
		}
	}
	
	public void fulfillResource(int id, Object resource) {
		synchronized (resources) {
			resources.put(id, resource);
			resources.notifyAll();
		}
	}
	
}
