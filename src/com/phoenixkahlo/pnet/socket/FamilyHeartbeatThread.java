package com.phoenixkahlo.pnet.socket;

import com.phoenixkahlo.util.EndableThread;

/**
 * Helper thread for a SocketFamily. Transmits heartbeat to all children every
 * heartbeat interval, and disconnects children who haven't received a heartbeat
 * in 3 times the interval.
 */
public class FamilyHeartbeatThread extends Thread implements EndableThread {

	private SocketFamily family;
	private volatile boolean shouldContinue = true;

	public FamilyHeartbeatThread(SocketFamily family) {
		this.family = family;
	}

	@Override
	public void run() {
		try {
			while (shouldContinue) {
				synchronized (family.getChildren()) {
					for (ChildSocket child : family.getChildren()) {
						child.sendHeartbeat();
						if (System.currentTimeMillis() - child.getLastHeartbeat() > SocketConstants.HEARTBEAT_INTERVAL
								* 3) {
							child.disconnect();
						}
					}
				}
				Thread.sleep(SocketConstants.HEARTBEAT_INTERVAL);
			}
		} catch (InterruptedException e) {
		}
	}

	@Override
	public void end() {
		shouldContinue = false;
		interrupt();
	}

}
