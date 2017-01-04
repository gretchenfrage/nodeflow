package tinkering;

import java.net.InetSocketAddress;
import java.util.Optional;

import com.phoenixkahlo.nodenet.BasicLocalNode;
import com.phoenixkahlo.nodenet.LocalNode;
import com.phoenixkahlo.nodenet.Node;

public class ConnectReturnTest {

	public static void main(String[] args) {
		new Thread(ConnectReturnTest::thread1).start();
		new Thread(ConnectReturnTest::thread2).start();
	}
	
	public static void thread1() {
		try {
			LocalNode net = new BasicLocalNode(23456);
			Thread.sleep(100);
			Optional<Node> remote = net.connect(new InetSocketAddress("localhost", 34567));
			System.out.println("connected: " + remote);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void thread2() {
		try {
			LocalNode net = new BasicLocalNode(34567);
			net.acceptAllIncoming();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
