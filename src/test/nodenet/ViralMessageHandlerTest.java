package test.nodenet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.phoenixkahlo.nodenet.ChildNode;
import com.phoenixkahlo.nodenet.LeaveJoinHandler;
import com.phoenixkahlo.nodenet.NeighborSetUpdate;
import com.phoenixkahlo.nodenet.NeighborSetUpdateTrigger;
import com.phoenixkahlo.nodenet.NetworkModel;
import com.phoenixkahlo.nodenet.NodeAddress;
import com.phoenixkahlo.nodenet.ViralMessage;
import com.phoenixkahlo.nodenet.ViralMessageHandler;
import com.phoenixkahlo.nodenet.ViralPayload;
import com.phoenixkahlo.nodenet.stream.ObjectStream;
import com.phoenixkahlo.ptest.Mockery;
import com.phoenixkahlo.ptest.Test;
import com.phoenixkahlo.ptest.Testing;

public class ViralMessageHandlerTest {

	public static void main(String[] args) {
		neighborSetUpdateTriggerTest();
	}

	@Test
	public static void test1() {
		ObjectStream str2 = Testing.mock(ObjectStream.class);
		ObjectStream str3 = Testing.mock(ObjectStream.class);
		ObjectStream str4 = Testing.mock(ObjectStream.class);
		Map<NodeAddress, ObjectStream> connections = new HashMap<>();
		connections.put(new NodeAddress(2), str2);
		connections.put(new NodeAddress(3), str3);
		connections.put(new NodeAddress(4), str4);

		NetworkModel model = new NetworkModel();
		model.connect(new NodeAddress(1), new NodeAddress(2));
		model.connect(new NodeAddress(1), new NodeAddress(3));
		model.connect(new NodeAddress(1), new NodeAddress(4));

		LeaveJoinHandler leaveJoinHandler = new LeaveJoinHandler(new NodeAddress(1), model, new HashMap<>(),
				address -> new ChildNode(null, connections, new NodeAddress(1), address));
		ViralMessageHandler handler = new ViralMessageHandler(new NodeAddress(1), connections, leaveJoinHandler, System.err);
		ViralMessage message = new ViralMessage(new NeighborSetUpdate(new NodeAddress(987234), new HashSet<>()));
		message.addInfected(new NodeAddress(2));

		// situation 1: 2 sends message, no others touched
		((Mockery) str3).method("send", Object.class).queueAssert(args -> args[0] == message);
		((Mockery) str4).method("send", Object.class).queueAssert(args -> args[0] == message);

		handler.handle(message);

		assert message.getInfected().contains(new NodeAddress(1));

		((Mockery) str3).method("send", Object.class).assertQueueEmpty();
		((Mockery) str4).method("send", Object.class).assertQueueEmpty();
	}

	@Test
	public static void test2() {
		ObjectStream str2 = Testing.mock(ObjectStream.class);
		ObjectStream str3 = Testing.mock(ObjectStream.class);
		ObjectStream str4 = Testing.mock(ObjectStream.class);
		Map<NodeAddress, ObjectStream> connections = new HashMap<>();
		connections.put(new NodeAddress(2), str2);
		connections.put(new NodeAddress(3), str3);
		connections.put(new NodeAddress(4), str4);

		NetworkModel model = new NetworkModel();
		model.connect(new NodeAddress(1), new NodeAddress(2));
		model.connect(new NodeAddress(1), new NodeAddress(3));
		model.connect(new NodeAddress(1), new NodeAddress(4));

		LeaveJoinHandler leaveJoinHandler = new LeaveJoinHandler(new NodeAddress(1), model, new HashMap<>(),
				address -> new ChildNode(null, connections, new NodeAddress(1), address));
		ViralMessageHandler handler = new ViralMessageHandler(new NodeAddress(1), connections, leaveJoinHandler, System.err);
		ViralMessage message = new ViralMessage(new NeighborSetUpdate(new NodeAddress(987234), new HashSet<>()));
		message.addInfected(new NodeAddress(2));
		message.addInfected(new NodeAddress(3));
		message.addInfected(new NodeAddress(4));

		// situation 2: 2 sends message, all others touched

		for (ObjectStream stream : connections.values()) {
			((Mockery) stream).method("send", Object.class).setResponse(args -> {
				System.out.println("ObjectStream.send(" + args[0] + ")");
				throw new AssertionError();
			});
		}

		handler.handle(message);

		assert message.getInfected().contains(new NodeAddress(1));

	}

	@Test
	public static void neighborSetUpdateTest() {
		Map<NodeAddress, ObjectStream> connections = new HashMap<>();

		NetworkModel model = new NetworkModel();

		Set<NodeAddress> connectedTo2 = new HashSet<>();
		connectedTo2.add(new NodeAddress(3));
		connectedTo2.add(new NodeAddress(4));
		connectedTo2.add(new NodeAddress(5));
		ViralPayload payload = new NeighborSetUpdate(new NodeAddress(2), connectedTo2);
		ViralMessage message = new ViralMessage(payload);

		LeaveJoinHandler leaveJoinHandler = new LeaveJoinHandler(new NodeAddress(1), model, new HashMap<>(),
				address -> new ChildNode(null, connections, new NodeAddress(1), address));
		ViralMessageHandler handler = new ViralMessageHandler(new NodeAddress(1), connections, leaveJoinHandler, System.err);

		handler.handle(message);

		assert model.connected(new NodeAddress(2), new NodeAddress(3));
		assert model.connected(new NodeAddress(2), new NodeAddress(4));
		assert model.connected(new NodeAddress(2), new NodeAddress(5));
	}

	@Test
	public static void neighborSetUpdateTriggerTest() {
		ObjectStream str2 = Testing.mock(ObjectStream.class);
		ObjectStream str3 = Testing.mock(ObjectStream.class);
		ObjectStream str4 = Testing.mock(ObjectStream.class);
		Map<NodeAddress, ObjectStream> connections = new HashMap<>();
		connections.put(new NodeAddress(2), str2);
		connections.put(new NodeAddress(3), str3);
		connections.put(new NodeAddress(4), str4);

		NetworkModel model = new NetworkModel();
		model.connect(new NodeAddress(1), new NodeAddress(2));
		model.connect(new NodeAddress(1), new NodeAddress(3));
		model.connect(new NodeAddress(1), new NodeAddress(4));

		ViralMessage message = new ViralMessage(new NeighborSetUpdateTrigger());
		message.addInfected(new NodeAddress(2));
		message.addInfected(new NodeAddress(3));
		message.addInfected(new NodeAddress(4));

		LeaveJoinHandler leaveJoinHandler = new LeaveJoinHandler(new NodeAddress(1), model, new HashMap<>(),
				address -> new ChildNode(null, connections, new NodeAddress(1), address));

		ViralMessageHandler handler = new ViralMessageHandler(new NodeAddress(1), connections, leaveJoinHandler, System.err);

		for (ObjectStream stream : connections.values()) {
			((Mockery) stream).method("send", Object.class)
					.queueAssert(args -> args[0] instanceof ViralMessage
							&& ((ViralMessage) args[0]).getPayload() instanceof NeighborSetUpdate
							&& ((NeighborSetUpdate) ((ViralMessage) args[0]).getPayload()).getNode()
									.equals(new NodeAddress(1))
							&& ((NeighborSetUpdate) ((ViralMessage) args[0]).getPayload()).getNeighbors()
									.equals(connections.keySet()));
		}

		handler.handle(message);
	}

}
