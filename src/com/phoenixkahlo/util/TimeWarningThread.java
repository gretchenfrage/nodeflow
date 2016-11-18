package com.phoenixkahlo.util;

/**
 * A thread that will wait a certain amount of time, then print a message to
 * System.err, unless it is interrupted before the timer completes.
 */
public class TimeWarningThread extends Thread {

	private String message;
	private long delay;

	/**
	 * Start self automatically.
	 */
	public TimeWarningThread(String message, long delay) {
		this.message = message;
		this.delay = delay;
		start();
	}

	@Override
	public void run() {
		try {
			Thread.sleep(delay);
			synchronized (System.err) {
				System.err.println(message);
			}
		} catch (InterruptedException e) {
		}
	}

}
