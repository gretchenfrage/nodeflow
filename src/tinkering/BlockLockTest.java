package tinkering;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class BlockLockTest {

	private static Object lock = new Object();
	
	public static void main(String[] args) {
		new Thread(BlockLockTest::thread1).start();
		new Thread(BlockLockTest::thread2).start();
	}
	
	public static void thread1() {
		BlockingQueue<Object> queue = new LinkedBlockingQueue<>();
		try {
			synchronized (lock) {
				System.out.println("thread1 dequeued: " + queue.take());
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void thread2() {
		try {
			Thread.sleep(100);
			synchronized (lock) {
				System.out.println("Hello world!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
