package test.util;

import com.phoenixkahlo.ptest.Test;
import com.phoenixkahlo.util.BlockingHashMap;
import com.phoenixkahlo.util.BlockingMap;

public class BlockingHashMapTest {

	public static void main(String[] args) {
		test1();
		test2();
		test3();
		test4();
	}
	
	@Test
	public static void test1() {
		BlockingMap<String, String> map = new BlockingHashMap<>();
		new Thread(() -> {
			try {
				Thread.sleep(500);
				map.put("hello", "world");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start();
		assert map.get("hello").equals("world");
	}
	
	@Test
	public static void test2() {
		BlockingMap<String, String> map = new BlockingHashMap<>();
		new Thread(() -> {
			try {
				Thread.sleep(500);
				map.put("hello", "world");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start();
		assert !map.tryGet("hello").isPresent();
	}
	
	@Test
	public static void test3() {
		BlockingMap<String, String> map = new BlockingHashMap<>();
		new Thread(() -> {
			try {
				Thread.sleep(500);
				map.put("hello", "world");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start();
		try {
			Thread.sleep(600);
			assert map.tryGet("hello").get().equals("world");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public static void test4() {
		BlockingMap<String, String> map = new BlockingHashMap<>();
		new Thread(() -> {
			try {
				Thread.sleep(500);
				map.put("hello", "world");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start();
		assert !map.tryGet("hello", 100).isPresent();
	}
	
	@Test
	public static void test5() {
		BlockingMap<String, String> map = new BlockingHashMap<>();
		new Thread(() -> {
			try {
				Thread.sleep(500);
				map.put("hello", "world");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start();
		assert map.tryGet("hello", 600).get().equals("world");
	}
	
}
