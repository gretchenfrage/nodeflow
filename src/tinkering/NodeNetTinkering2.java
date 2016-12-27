package tinkering;

import java.net.InetSocketAddress;

import com.phoenixkahlo.nodenet.BasicLocalNode;
import com.phoenixkahlo.nodenet.LocalNode;
import com.phoenixkahlo.nodenet.serialization.StringSerializer;

public class NodeNetTinkering2 {

	public static void main(String[] args) {
		new Thread(NodeNetTinkering2::thread1).start();
		new Thread(NodeNetTinkering2::thread2).start();
		new Thread(NodeNetTinkering2::thread3).start();
		new Thread(NodeNetTinkering2::thread4).start();
		new Thread(NodeNetTinkering2::thread5).start();
	}
	
	public static void init(LocalNode net) {
		net.addSerializer(new StringSerializer(), 1);
	}
	
	public static void thread1() {
		try {
			LocalNode net = new BasicLocalNode(3001);
			init(net);
			net.acceptAllIncoming();
			
			Thread.sleep(100);
			
			net.connect(new InetSocketAddress("localhost", 3001));
			net.connect(new InetSocketAddress("localhost", 3002));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void thread2() {
		try {
			LocalNode net = new BasicLocalNode(3002);
			init(net);
			net.acceptAllIncoming();
			
			Thread.sleep(100);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void thread3() {
		try {
			LocalNode net = new BasicLocalNode(3003);
			init(net);
			net.acceptAllIncoming();
			
			Thread.sleep(100);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void thread4() {
		try {
			LocalNode net = new BasicLocalNode(3004);
			init(net);
			net.acceptAllIncoming();
			
			Thread.sleep(100);
			
			net.connect(new InetSocketAddress("localhost", 3001));
			net.connect(new InetSocketAddress("localhost", 3002));
			net.connect(new InetSocketAddress("localhost", 3005));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void thread5() {
		try {
			LocalNode net = new BasicLocalNode(3005);
			init(net);
			net.acceptAllIncoming();
			
			Thread.sleep(100);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
