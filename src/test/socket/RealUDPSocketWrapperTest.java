package test.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;

import com.phoenixkahlo.pnet.socket.RealUDPSocketWrapper;
import com.phoenixkahlo.pnet.socket.UDPSocketWrapper;
import com.phoenixkahlo.ptest.Test;

public class RealUDPSocketWrapperTest {

	@Test
	public static void test1() {
		byte[] bin = {3, 6, 1, 7, 2, 76, 1, 3};
		
		new Thread(() -> {
			try {
				UDPSocketWrapper wrapper = new RealUDPSocketWrapper(34567);
				byte[] buffer = new byte[bin.length];
				SocketAddress address = wrapper.receive(buffer);
				assert Arrays.equals(buffer, bin);
				assert address.toString().substring(0, 11).equals("/127.0.0.1:");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
		
		new Thread(() -> {
			try {
				UDPSocketWrapper wrapper = new RealUDPSocketWrapper();
				wrapper.send(bin, new InetSocketAddress("localhost", 34567));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}
	
}
