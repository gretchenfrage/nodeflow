package test.socket;

import static com.phoenixkahlo.pnet.serialization.SerializationUtils.bytesToInt;
import static com.phoenixkahlo.pnet.serialization.SerializationUtils.bytesToShort;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;

import com.phoenixkahlo.pnet.socket.BasicChildSocket;
import com.phoenixkahlo.pnet.socket.BasicMessageBuilder;
import com.phoenixkahlo.pnet.socket.ChildSocket;
import com.phoenixkahlo.pnet.socket.ReceivedPayload;
import com.phoenixkahlo.pnet.socket.SocketFamily;
import com.phoenixkahlo.pnet.socket.UDPSocketWrapper;
import com.phoenixkahlo.ptest.Mockery;
import com.phoenixkahlo.ptest.Test;
import com.phoenixkahlo.ptest.Testing;

public class BasicChildSocketTest {

	@Test
	public static void test1() throws IOException {
		SocketFamily family = Testing.mock(SocketFamily.class);
		int connectionID = 9283765;
		SocketAddress sendTo = new InetSocketAddress("localhost", 42684);

		ChildSocket socket = new BasicChildSocket(family, connectionID, sendTo, BasicMessageBuilder::new);

		/*
		 * Test the transmission of a simple send
		 */
		System.out.println("* subtest1 *");
		byte[] sendTest1 = { 1, 6, 1, 3, 7, 1, 4, 67, 2, 3 };
		UDPSocketWrapper wrapper = Testing.mock(UDPSocketWrapper.class);
		((Mockery) family).method("getUDPWrapper").setResponse(wrapper);
		((Mockery) wrapper).method("send", byte[].class, SocketAddress.class).queueResponse(args -> {
			byte[] arr = (byte[]) args[0];
			assert arr[12] == 0;
			assert arr[13] == 1;
			assert bytesToShort(new byte[] { arr[14], arr[15] }) == 10;
			for (int i = 0; i < 10; i++) {
				assert arr[i + 16] == sendTest1[i];
			}
			return null;
		});
		socket.send(sendTest1);
		((Mockery) wrapper).method("send", byte[].class, SocketAddress.class).assertQueueEmpty();

		/*
		 * Test the transmissions of a series of 3 sendOrdereds
		 */
		System.out.println("* subtest2 *");
		byte[] sendTest2 = { 3, 6, 1, 2, 6, 9, 2, 4, 7, 2 };
		((Mockery) wrapper).method("send", byte[].class, SocketAddress.class).queueResponse(args -> {
			byte[] arr = (byte[]) args[0];
			assert bytesToInt(new byte[] {arr[12], arr[13], arr[14], arr[15]}) == 0;
			assert arr[16] == 0;
			assert arr[17] == 1;
			assert bytesToShort(new byte[] { arr[18], arr[19] }) == 10;
			for (int i = 0; i < 10; i++) {
				assert arr[i + 20] == sendTest2[i];
			}
			return null;
		});
		((Mockery) wrapper).method("send", byte[].class, SocketAddress.class).queueResponse(args -> {
			byte[] arr = (byte[]) args[0];
			assert bytesToInt(new byte[] {arr[12], arr[13], arr[14], arr[15]}) == 1;
			assert arr[16] == 0;
			assert arr[17] == 1;
			assert bytesToShort(new byte[] { arr[18], arr[19] }) == 10;
			for (int i = 0; i < 10; i++) {
				assert arr[i + 20] == sendTest2[i];
			}
			return null;
		});
		((Mockery) wrapper).method("send", byte[].class, SocketAddress.class).queueResponse(args -> {
			byte[] arr = (byte[]) args[0];
			assert bytesToInt(new byte[] {arr[12], arr[13], arr[14], arr[15]}) == 2;
			assert arr[16] == 0;
			assert arr[17] == 1;
			assert bytesToShort(new byte[] { arr[18], arr[19] }) == 10;
			for (int i = 0; i < 10; i++) {
				assert arr[i + 20] == sendTest2[i];
			}
			return null;
		});
		socket.sendOrdered(sendTest2);
		socket.sendOrdered(sendTest2);
		socket.sendOrdered(sendTest2);
		((Mockery) wrapper).method("send", byte[].class, SocketAddress.class).assertQueueEmpty();
		
		/*
		 * Test that it can receive messages from receivePayload, blocking as necessary
		 */
		System.out.println("* subtest3 *");
		byte[] receiveTest1 = {1, 3, 6, 1, 3, 6, 7, 2};
		byte[] receiveTest2 = {1, 6, 2, 7, 2, 3, 6, 2, 76};
		((Mockery) wrapper).method("send", byte[].class, SocketAddress.class).expectResponse();
		((Mockery) wrapper).method("send", byte[].class, SocketAddress.class).expectResponse();
		socket.receivePayload(new ReceivedPayload(65465487, 984654, (byte) 0, (byte) 1, receiveTest1));
		new Thread(() -> {
			try {
				Thread.sleep(20);
				socket.receivePayload(new ReceivedPayload(684948, 9876251, (byte) 0, (byte) 1, receiveTest2));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start();
		assert Arrays.equals(socket.receive(), receiveTest1);
		assert Arrays.equals(socket.receive(), receiveTest2);
	}

}
