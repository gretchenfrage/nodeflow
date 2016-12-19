package tinkering;

import java.net.InetSocketAddress;
import java.util.Arrays;

import com.phoenixkahlo.nodenet.stream.BasicStreamFamily;
import com.phoenixkahlo.nodenet.stream.DatagramStream;
import com.phoenixkahlo.nodenet.stream.StreamFamily;

public class PNetSocketTinkering {

	public static void main(String[] args) {
		byte[] bin1 = { 1, 6, 3, 6, 2, 7, 9, 3, 8 };
		byte[] bin2 = { 89, 4, 7, 43, 3, 3, 6, 8, 5, 4 };

		new Thread(() -> {
			try {
				StreamFamily family = new BasicStreamFamily(34567);
				family.setReceiver(p -> true, socket -> new Thread(() -> {
					try {
						// not receiving pulses
						synchronized (System.out) {
							System.out.println("received socket " + socket);
						}

						System.out.println(Arrays.toString(socket.receive()));

					} catch (Exception e) {
						e.printStackTrace();
					}
				}).start());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
		new Thread(() -> {
			try {

				Thread.sleep(20);
				StreamFamily family = new BasicStreamFamily(65432);
				DatagramStream socket = family.connect(new InetSocketAddress("localhost", 34567)).get();
				synchronized (System.out) {
					System.out.println("connected socket: " + socket);
				}

				socket.send(bin1);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
	}

}
