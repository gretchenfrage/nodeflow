package tinkering;

import java.net.InetSocketAddress;

import com.phoenixkahlo.nodenet.BasicLocalNode;
import com.phoenixkahlo.nodenet.LocalNode;
import com.phoenixkahlo.nodenet.NodeAddress;
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
	
	private static volatile NodeAddress address1;
	private static volatile NodeAddress address2;
	private static volatile NodeAddress address3;
	private static volatile NodeAddress address4;
	private static volatile NodeAddress address5;
	
	public static void thread1() {
		try {
			LocalNode net = new BasicLocalNode(3001);
			address1 = net.getAddress();
			init(net);
			net.acceptAllIncoming();
			
			Thread.sleep(100);
			
			net.connect(new InetSocketAddress("localhost", 3002));
			net.connect(new InetSocketAddress("localhost", 3003));
			
			Thread.sleep(100);
			Thread.sleep(100);
			
			System.out.println("net1.nodes=" + net.getNodes());
			
			net.getNode(address5).get().send("hello world!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void thread2() {
		try {
			LocalNode net = new BasicLocalNode(3002);
			address2 = net.getAddress();
			init(net);
			net.acceptAllIncoming();
			
			Thread.sleep(100);
			Thread.sleep(100);
			Thread.sleep(100);
			
			System.out.println("net2.nodes=" + net.getNodes());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void thread3() {
		try {
			LocalNode net = new BasicLocalNode(3003);
			address3 = net.getAddress();
			init(net);
			net.acceptAllIncoming();
			
			Thread.sleep(100);
			Thread.sleep(100);
			Thread.sleep(100);
			
			System.out.println("net3.nodes=" + net.getNodes());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void thread4() {
		try {
			LocalNode net = new BasicLocalNode(3004);
			address4 = net.getAddress();
			init(net);
			net.acceptAllIncoming();
			
			Thread.sleep(100);
			
			net.connect(new InetSocketAddress("localhost", 3002));
			net.connect(new InetSocketAddress("localhost", 3003));
			net.connect(new InetSocketAddress("localhost", 3005));
			
			Thread.sleep(100);
			Thread.sleep(100);
			
			System.out.println("net4.nodes=" + net.getNodes());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void thread5() {
		try {
			LocalNode net = new BasicLocalNode(3005);
			address5 = net.getAddress();
			init(net);
			net.acceptAllIncoming();
			
			Thread.sleep(100);
			Thread.sleep(100);
			//((BasicLocalNode) net).triggerupdate();
			Thread.sleep(100);
			
			System.out.println("net5.nodes=" + net.getNodes());
			System.out.println("net5 received: " + net.getNode(address1).get().receive());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
