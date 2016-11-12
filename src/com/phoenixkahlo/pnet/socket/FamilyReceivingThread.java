package com.phoenixkahlo.pnet.socket;

import com.phoenixkahlo.util.EndableThread;

public class FamilyReceivingThread extends Thread implements EndableThread {

	private SocketFamily family;
	
	public FamilyReceivingThread(SocketFamily family) {
		this.family = family;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

	@Override
	public void end() {
		// TODO Auto-generated method stub

	}

}
