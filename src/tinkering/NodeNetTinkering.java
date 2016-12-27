package tinkering;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.function.BiConsumer;

import com.phoenixkahlo.nodenet.BasicLocalNode;
import com.phoenixkahlo.nodenet.DisconnectionException;
import com.phoenixkahlo.nodenet.LocalNode;
import com.phoenixkahlo.nodenet.Node;
import com.phoenixkahlo.nodenet.serialization.Serializer;
import com.phoenixkahlo.nodenet.serialization.StringSerializer;

public class NodeNetTinkering {

	public static void main(String[] args) {
		new Thread(NodeNetTinkering::thread1).start();
		new Thread(NodeNetTinkering::thread2).start();
	}
	
	public static void init(BiConsumer<Serializer, Integer> registrar) {
		registrar.accept(new StringSerializer(), 1);
	}

	public static void thread1() {
		try {
			LocalNode network = new BasicLocalNode(23458);
			init(network::addSerializer);
			network.listenForJoin(node -> new Thread(() -> {
				try {
					System.out.println("connection formed from thread1");
					System.out.println(network.getNodes());
					node.send("hello world");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {}
					node.send("goodbye world");
				} catch (DisconnectionException e) {
					e.printStackTrace();
				}
			}).start());
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
			init(network::addSerializer);
			Optional<Node> optionalNode = network.connect(new InetSocketAddress("localhost", 23458));
			if (optionalNode.isPresent()) {
				Node node = optionalNode.get();
				System.out.println("connection formed from thread2");
				Object received = node.receive();
				System.out.println("received " + received);
				network.disconnect();
				System.out.println("disconnected");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
