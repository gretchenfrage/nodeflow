package test.nodenet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.phoenixkahlo.nodenet.AddressedAttemptSequence;
import com.phoenixkahlo.nodenet.AddressedMessage;
import com.phoenixkahlo.nodenet.NetworkModel;
import com.phoenixkahlo.nodenet.NodeAddress;
import com.phoenixkahlo.nodenet.stream.ObjectStream;
import com.phoenixkahlo.ptest.Test;

public class AddressedAttemptSequenceTest {

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

		Map<NodeAddress, ObjectStream> connections = new HashMap<>();
		connections.put(new NodeAddress(2), null);
		connections.put(new NodeAddress(3), null);
		connections.put(new NodeAddress(4), null);

		AddressedMessage message = new AddressedMessage(null, new NodeAddress(8));

		Iterator<NodeAddress> sequence = new AddressedAttemptSequence(model, message, new NodeAddress(1), connections);
		assert sequence.next().equals(new NodeAddress(2));
		assert sequence.next().equals(new NodeAddress(3));
		assert sequence.next().equals(new NodeAddress(4));
		assert !sequence.hasNext();
	}

}
