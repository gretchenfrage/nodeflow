package com.phoenixkahlo.pnet.socket;

import com.phoenixkahlo.util.EndableThread;

/**
 * Helper thread for a socket family. Occasionally invokes retransmitUnconfirmed
 * in all children.
 */
public class FamilyRetransmissionThread extends Thread implements EndableThread {

	private SocketFamily family;
	private volatile boolean shouldContinue = true;
	
	public FamilyRetransmissionThread(SocketFamily family) {
		this.family = family;
	}

	@Override
	public void run() {
		while (shouldContinue) {
			synchronized (family.getChildren()) {
				for (ChildSocket socket : family.getChildren()) {
					socket.retransmitUnconfirmed();
				}
			}
		}
	}

	@Override
	public void end() {
		shouldContinue = false;
		interrupt();
	}

}
