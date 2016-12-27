package tinkering;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Optional;

import com.phoenixkahlo.nodenet.*;

public class NodeNetTinkering {

	public static void main(String[] args) {
		new Thread(NodeNetTinkering::thread1).start();
		new Thread(NodeNetTinkering::thread2).start();
	}

	public static void thread1() {
		try {
			LocalNode network = new BasicLocalNode(23458);
			network.setGreeter(address -> {
				boolean accept = true; // address.getAddress().isAnyLocalAddress();
				System.out.println("connection proposed from " + address + (accept ? " - accepting" : " - rejecting"));
				return accept;
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void thread2() {
		try {
			Thread.sleep(1_000);
			LocalNode network = new BasicLocalNode();
			Optional<Node> optionalNode = network.connect(new InetSocketAddress("localhost", 23458));
			if (optionalNode.isPresent()) {
				Node node = optionalNode.get();
				System.out.println("connection formed");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
