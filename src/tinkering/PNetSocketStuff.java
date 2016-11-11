package tinkering;

import java.net.InetAddress;

import com.phoenixkahlo.pnet.socket.PNetSocket;
import com.phoenixkahlo.pnet.socket.SocketFamily;

public class PNetSocketStuff {

	public static void main(String[] args) {
		new Thread(() -> {
			try {
				
				SocketFamily family = new SocketFamily(34567);
				family.setReceiver(p -> true, socket -> {
					
					
					
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
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
	}

}
