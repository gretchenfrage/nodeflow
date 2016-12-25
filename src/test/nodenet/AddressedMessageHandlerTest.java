package test.nodenet;

import java.util.HashMap;
import java.util.Map;

import com.phoenixkahlo.nodenet.NetworkModel;
import com.phoenixkahlo.nodenet.Node;
import com.phoenixkahlo.nodenet.NodeAddress;
import com.phoenixkahlo.nodenet.stream.ObjectStream;
import com.phoenixkahlo.ptest.Test;
import com.phoenixkahlo.ptest.Testing;

public class AddressedMessageHandlerTest {

	@Test
	public static void test1() {
		NetworkModel model = new NetworkModel();
		model.connect(new NodeAddress(1), new NodeAddress(2));
		model.connect(new NodeAddress(1), new NodeAddress(3));
		model.connect(new NodeAddress(1), new NodeAddress(4));
		model.connect(new NodeAddress(3), new NodeAddress(5));
		model.connect(new NodeAddress(4), new NodeAddress(6));
		model.connect(new NodeAddress(6), new NodeAddress(7));
		model.connect(new NodeAddress(2), new NodeAddress(8));
		model.connect(new NodeAddress(5), new NodeAddress(8));
		model.connect(new NodeAddress(7), new NodeAddress(8));
		
		Map<NodeAddress, Node> nodes = new HashMap<>();
		for (int n = 1; n <= 8; n++) {
			nodes.put(new NodeAddress(n), Testing.mock(Node.class));
		}
		
		ObjectStream stream1 = Testing.mock(ObjectStream.class);
		ObjectStream stream2 = Testing.mock(ObjectStream.class);
		ObjectStream stream3 = Testing.mock(ObjectStream.class);
		
	}
	
}
