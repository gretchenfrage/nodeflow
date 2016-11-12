package com.phoenixkahlo.pnet.socket;

import com.phoenixkahlo.util.EndableThread;

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
								* 2) {
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
