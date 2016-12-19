package com.phoenixkahlo.pnet.socket;

import com.phoenixkahlo.util.EndableThread;

/**
 * Helper thread for a StreamFamily. Transmits heartbeat to all children every
 * heartbeat interval, and disconnects children who haven't received a heartbeat
 * in 3 times the interval.
 */
public class FamilyHeartbeatThread extends Thread implements EndableThread {

	private StreamFamily family;
	private volatile boolean shouldContinue = true;

	public FamilyHeartbeatThread(StreamFamily family) {
		this.family = family;
	}

	@Override
	public void run() {
		try {
			while (shouldContinue) {
				synchronized (family.getChildren()) {
					long currentTime = System.currentTimeMillis();
					for (int i = 0; i < family.getChildren().size(); i++) {
						ChildStream child = family.getChildren().get(i);
						child.sendHeartbeat();
						if (currentTime - child.getTimeOfCreation() > DatagramStreamConfig.HEARTBEAT_INTERVAL * 3
								&& currentTime - child.getLastHeartbeat() > DatagramStreamConfig.HEARTBEAT_INTERVAL * 3) {
							child.disconnect();
						}
					}
				}
				Thread.sleep(DatagramStreamConfig.HEARTBEAT_INTERVAL);
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
