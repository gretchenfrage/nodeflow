package tinkering;

import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.phoenixkahlo.nodenet.BasicLocalNode;
import com.phoenixkahlo.nodenet.LocalNode;
import com.phoenixkahlo.nodenet.Node;
import com.phoenixkahlo.nodenet.proxy.Proxy;
import com.phoenixkahlo.nodenet.serialization.ThrowableSerializer;

public class ProxyTinkering {

	public static interface Bot {

		void say(String str);

		String getName();

		void hell() throws UnsupportedOperationException;

	}

	public static class LocalBot implements Bot {

		private String name;

		public LocalBot(String name) {
			this.name = name;
		}

		@Override
		public void say(String str) {
			System.out.println(name + ": " + str);
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public void hell() throws UnsupportedOperationException {
			throw new UnsupportedOperationException("D=");
		}

	}

	public static void main(String[] args) {
		new Thread(ProxyTinkering::thread1).start();
		new Thread(ProxyTinkering::thread2).start();
	}

	public static void init(LocalNode net) {
		net.addSerializer(new ThrowableSerializer<>(UnsupportedOperationException.class,
				UnsupportedOperationException::new, null), 1);
	}

	public static void thread1() {
		try {
			LocalNode net = new BasicLocalNode(35542);

			init(net);

			BlockingQueue<Node> joined = new LinkedBlockingQueue<>();
			net.listenForJoin(joined::add);
			net.acceptAllIncoming();

			Node other = joined.take();

			Bot bot = new LocalBot("gerold");
			Proxy<Bot> proxy = net.makeProxy(bot, Bot.class);
			other.send(proxy);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void thread2() {
		try {
			LocalNode net = new BasicLocalNode();

			init(net);

			Node other = net.connect(new InetSocketAddress("localhost", 35542)).get();

			Proxy<Bot> proxy = ((Proxy<?>) other.receive()).cast(Bot.class);
			System.out.println("got proxy: " + proxy);

			proxy.unblocking(true).say("hello from the other side");

			System.out.println("remote name is: " + proxy.blocking().getName());

			try {
				proxy.blocking().hell();
			} catch (UnsupportedOperationException e) {
				System.out.println("caught " + e);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
