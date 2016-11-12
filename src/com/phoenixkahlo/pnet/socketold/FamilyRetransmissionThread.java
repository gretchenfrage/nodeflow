package com.phoenixkahlo.pnet.socketold;

import java.io.IOException;

/**
 * A thread that belong to a SocketFamily which is responsible for detecting
 * when payload transmissions have been unconfirmed for an unacceptable amount
 * of time and making the appropriate retransmissions.
 */
@Deprecated
public class FamilyRetransmissionThread extends Thread {

	private SocketFamily family;
	private volatile boolean shouldContinue = true;

	public FamilyRetransmissionThread(SocketFamily family) {
		this.family = family;
	}

	@Override
	public void run() {
		while (shouldContinue) {
			synchronized (family.getChildren()) {
				for (ChildSocket child : family.getChildren()) {
					try {
						child.retransmitUnconfirmed();
					} catch (IOException e) {
					}
				}
			}
			try {
				Thread.sleep(SocketConstants.RETRANSMISSION_INTERVAL / 3);
			} catch (InterruptedException e) {
			}
		}
	}

	public void kill() {
		shouldContinue = false;
		interrupt();
	}

}
