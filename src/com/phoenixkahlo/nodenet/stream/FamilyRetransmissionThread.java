package com.phoenixkahlo.nodenet.stream;

import com.phoenixkahlo.util.EndableThread;

/**
 * Helper thread for a socket family. Occasionally invokes retransmitUnconfirmed
 * in all children.
 */
public class FamilyRetransmissionThread extends Thread implements EndableThread {

	private StreamFamily family;
	private volatile boolean shouldContinue = true;
	
	public FamilyRetransmissionThread(StreamFamily family) {
		this.family = family;
	}

	@Override
	public void run() {
		while (shouldContinue) {
			synchronized (family.getChildren()) {
				for (ChildStream socket : family.getChildren()) {
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
