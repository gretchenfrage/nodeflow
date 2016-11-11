package com.phoenixkahlo.pnet.socket;

import java.io.IOException;

/**
 * A thread that belongs to a SocketFamily which is responsible for broadcasting
 * heartbeats, and disconnecting sockets that haven't received a heartbeat for
 * an unacceptable amount of time.
 */
public class FamilyHeartbeatThread extends Thread {

	private SocketFamily family;
	private volatile boolean shouldContinue = true;

	public FamilyHeartbeatThread(SocketFamily family) {
		this.family = family;
	}

	@Override
	public void run() {
		while (shouldContinue) {
			synchronized (family.getChildren()) {
				for (ChildSocket child : family.getChildren()) {
					try {
						child.sendHeartbeat();
					} catch (IOException e) {
					}
					if (System.currentTimeMillis() - child.getLastHeartbeatTime() > SocketConstants.HEARTBEAT_INTERVAL * 2) {
						child.disconnect();
					}
				}
			}
			try {
				Thread.sleep(SocketConstants.HEARTBEAT_INTERVAL);
			} catch (InterruptedException e) {
			}
		}
	}

	public void kill() {
		shouldContinue = false;
		interrupt();
	}

}
