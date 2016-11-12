package tinkering;

import java.net.InetAddress;
import java.util.Arrays;

import com.phoenixkahlo.pnet.socketold.PNetSocket;
import com.phoenixkahlo.pnet.socketold.SocketFamily;

@SuppressWarnings("deprecation")
public class PNetSocketStuff {

	public static void main(String[] args) {
		byte[] bin = {1, 2, 3, 4, 5, 6, 7, 8, 9};
		
		new Thread(() -> {
			try {
				
				SocketFamily family = new SocketFamily(34567);
				family.setReceiver(p -> true, socket -> {
					
					try {
						
						socket.send(bin);
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				});
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
		
		new Thread(() -> {
			try {
				
				SocketFamily family = new SocketFamily();
				Thread.sleep(200);
				PNetSocket socket = family.connect(InetAddress.getLocalHost(), 34567).get();
				
				System.out.println(Arrays.toString(socket.receive()));
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
	}

}
