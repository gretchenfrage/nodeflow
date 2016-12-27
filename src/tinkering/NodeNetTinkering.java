package tinkering;

import java.net.SocketException;

import com.phoenixkahlo.nodenet.BasicLocalNode;
import com.phoenixkahlo.nodenet.LocalNode;

public class NodeNetTinkering {

	public static void main(String[] args) {
		new Thread(() -> {
			try {
				LocalNode network = new BasicLocalNode(23458);
				network.setGreeter(address -> {
					
					return false;
				});
			} catch (SocketException e) {
				e.printStackTrace();
			}
		}).start();
		new Thread(() -> {
			
		}).start();
	}

}
