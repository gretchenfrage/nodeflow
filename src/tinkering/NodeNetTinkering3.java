package tinkering;

import java.net.InetSocketAddress;
import java.util.Optional;

import com.phoenixkahlo.nodenet.BasicLocalNode;
import com.phoenixkahlo.nodenet.LocalNode;
import com.phoenixkahlo.nodenet.Node;
import com.phoenixkahlo.nodenet.NodeAddress;
import com.phoenixkahlo.nodenet.serialization.StringSerializer;

public class NodeNetTinkering3 {

	public static void main(String[] args) {
		new Thread(NodeNetTinkering3::thread1).start();
		new Thread(NodeNetTinkering3::thread2).start();
		new Thread(NodeNetTinkering3::thread3).start();
	}

	public static void init(LocalNode net) {
		net.addSerializer(new StringSerializer(), 1);
	}

	private static volatile NodeAddress address1;
	private static volatile NodeAddress address3;

	public static void thread1() {
		try {
			// t=0ms: intiailization
			LocalNode net = new BasicLocalNode(3001);
			init(net);
			net.acceptAllIncoming();
			address1 = net.getAddress();
			System.out.println("net1 initialized");

			// t=100ms: 1 connects to 2
			Thread.sleep(1000);
			Optional<Node> node2 = net.connect(new InetSocketAddress("localhost", 3002));
			System.out.println("net1 connected to node2: " + node2);

			// t=300ms: print out stuff
			Thread.sleep(2000);
			System.out.println("net1.address=" + net.getAddress());
			System.out.println("net1.nodes=" + net.getNodes());

			// t=400ms: node 1 sends message to node3
			Thread.sleep(100);
			net.getNode(address3).get().send("hello world!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void thread2() {
		try {
			// t=0ms: initialization
			LocalNode net = new BasicLocalNode(3002);
			init(net);
			net.acceptAllIncoming();
			System.out.println("net2 initialized");

			// t=200ms: 2 connects to 3
			Thread.sleep(2000);
			Optional<Node> node3 = net.connect(new InetSocketAddress("localhost", 3003));
			System.out.println("net2 connected to node3: " + node3);

			// t=300ms: print out stuff
			Thread.sleep(1000);
			System.out.println("net2.address=" + net.getAddress());
			System.out.println("net2.nodes=" + net.getNodes());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void thread3() {
		try {
			// t=0ms: initialization
			LocalNode net = new BasicLocalNode(3003);
			init(net);
			net.acceptAllIncoming();
			address3 = net.getAddress();
			System.out.println("net3 initialized");

			// t=300ms: print out stuff
			Thread.sleep(3000);
			System.out.println("net3.address=" + net.getAddress());
			System.out.println("net3.nodes=" + net.getNodes());

			// t=350ms: node 3 receives message from node 1
			Thread.sleep(50);
			System.out.println("node 3 receives: " + net.getNode(address1).get().receive());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
