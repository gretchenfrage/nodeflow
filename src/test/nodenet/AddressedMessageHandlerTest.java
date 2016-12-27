package test.nodenet;

import java.util.HashMap;
import java.util.Map;

import com.phoenixkahlo.nodenet.AddressedMessage;
import com.phoenixkahlo.nodenet.AddressedMessageHandler;
import com.phoenixkahlo.nodenet.AddressedMessageResult;
import com.phoenixkahlo.nodenet.AddressedPayload;
import com.phoenixkahlo.nodenet.ChildNode;
import com.phoenixkahlo.nodenet.ClientTransmission;
import com.phoenixkahlo.nodenet.DisconnectionException;
import com.phoenixkahlo.nodenet.NetworkModel;
import com.phoenixkahlo.nodenet.NodeAddress;
import com.phoenixkahlo.nodenet.stream.ObjectStream;
import com.phoenixkahlo.ptest.MethodMocker;
import com.phoenixkahlo.ptest.Mockery;
import com.phoenixkahlo.ptest.Test;
import com.phoenixkahlo.ptest.Testing;

public class AddressedMessageHandlerTest {

	public static void main(String[] args) {
		handlePayloadTest();
	}
	
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
		model.connect(new NodeAddress(1), new NodeAddress(-1));

		Map<NodeAddress, ChildNode> nodes = new HashMap<>();

		ObjectStream stream1 = Testing.mock(ObjectStream.class);
		ObjectStream stream2 = Testing.mock(ObjectStream.class);
		ObjectStream stream3 = Testing.mock(ObjectStream.class);
		ObjectStream returnStream = Testing.mock(ObjectStream.class);
		Map<NodeAddress, ObjectStream> connections = new HashMap<>();
		connections.put(new NodeAddress(2), stream1);
		connections.put(new NodeAddress(3), stream2);
		connections.put(new NodeAddress(4), stream3);
		connections.put(new NodeAddress(-1), returnStream);

		AddressedMessage message = new AddressedMessage(null, new NodeAddress(-1),  new NodeAddress(8));
		
		AddressedMessageHandler handler = new AddressedMessageHandler(new NodeAddress(1), model, connections, nodes);

		// Scenario 1: node 2 succeeds
		((Mockery) stream1).method("send", Object.class).addResponse(args -> args[0] == message, args -> {
			int transmissionID = ((AddressedMessage) args[0]).getTransmissionID();
			new Thread(() -> {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
				handler.handle(new AddressedMessageResult(transmissionID, true));
			}).start();
			return null;
		});
		((Mockery) stream2).method("send", Object.class).setResponse(args -> null);
		((Mockery) stream3).method("send", Object.class).setResponse(args -> null);
		((Mockery) returnStream).method("send", Object.class).queueAssert(args -> {
			return args[0] instanceof AddressedMessageResult && ((AddressedMessageResult) args[0]).wasSuccessful();
		});

		handler.handle(message, new NodeAddress(-1));

		try {
			Thread.sleep(3_000);
		} catch (InterruptedException e) {
		}

		((Mockery) returnStream).method("send", Object.class).assertQueueEmpty();
	}
	
	@Test
	public static void test2() {
		
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
		model.connect(new NodeAddress(1), new NodeAddress(-1));

		Map<NodeAddress, ChildNode> nodes = new HashMap<>();

		ObjectStream stream1 = Testing.mock(ObjectStream.class);
		ObjectStream stream2 = Testing.mock(ObjectStream.class);
		ObjectStream stream3 = Testing.mock(ObjectStream.class);
		ObjectStream returnStream = Testing.mock(ObjectStream.class);
		Map<NodeAddress, ObjectStream> connections = new HashMap<>();
		connections.put(new NodeAddress(2), stream1);
		connections.put(new NodeAddress(3), stream2);
		connections.put(new NodeAddress(4), stream3);
		connections.put(new NodeAddress(-1), returnStream);

		AddressedMessage message = new AddressedMessage(null, new NodeAddress(-1),  new NodeAddress(8));
		
		AddressedMessageHandler handler = new AddressedMessageHandler(new NodeAddress(1), model, connections, nodes);

		// Scenario 2: node 2 fails, node 3 fails, node 4 succeeds
		((Mockery) stream1).method("send", Object.class).addResponse(args -> args[0] == message, args -> {
			int transmissionID = ((AddressedMessage) args[0]).getTransmissionID();
			new Thread(() -> {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
				handler.handle(new AddressedMessageResult(transmissionID, false));
			}).start();
			return null;
		});
		((Mockery) stream2).method("send", Object.class).addResponse(args -> args[0] == message, args -> {
			int transmissionID = ((AddressedMessage) args[0]).getTransmissionID();
			new Thread(() -> {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
				handler.handle(new AddressedMessageResult(transmissionID, false));
			}).start();
			return null;
		});
		((Mockery) stream3).method("send", Object.class).addResponse(args -> args[0] == message, args -> {
			int transmissionID = ((AddressedMessage) args[0]).getTransmissionID();
			new Thread(() -> {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
				handler.handle(new AddressedMessageResult(transmissionID, true));
			}).start();
			return null;
		});
		((Mockery) returnStream).method("send", Object.class).queueAssert(args -> {
			return args[0] instanceof AddressedMessageResult && ((AddressedMessageResult) args[0]).wasSuccessful();
		});

		handler.handle(message, new NodeAddress(-1));

		try {
			Thread.sleep(3_000);
		} catch (InterruptedException e) {
		}

		((Mockery) returnStream).method("send", Object.class).assertQueueEmpty();
	}
	
	@Test
	public static void test3() {
		
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
		model.connect(new NodeAddress(1), new NodeAddress(-1));

		Map<NodeAddress, ChildNode> nodes = new HashMap<>();

		ObjectStream stream1 = Testing.mock(ObjectStream.class);
		ObjectStream stream2 = Testing.mock(ObjectStream.class);
		ObjectStream stream3 = Testing.mock(ObjectStream.class);
		ObjectStream returnStream = Testing.mock(ObjectStream.class);
		Map<NodeAddress, ObjectStream> connections = new HashMap<>();
		connections.put(new NodeAddress(2), stream1);
		connections.put(new NodeAddress(3), stream2);
		connections.put(new NodeAddress(4), stream3);
		connections.put(new NodeAddress(-1), returnStream);

		AddressedMessage message = new AddressedMessage(null, new NodeAddress(-1),  new NodeAddress(8));
		
		AddressedMessageHandler handler = new AddressedMessageHandler(new NodeAddress(1), model, connections, nodes);

		// Scenario 3: node 2 responds with success 100ms after node 3 receives its message
		Object notary = new Object();
		((Mockery) stream1).method("send", Object.class).addResponse(args -> args[0] == message, args -> {
			int transmissionID = ((AddressedMessage) args[0]).getTransmissionID();
			new Thread(() -> {
				try {
					synchronized (notary) {
						notary.wait();
					}
				} catch (InterruptedException e1) {
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
				handler.handle(new AddressedMessageResult(transmissionID, true));
			}).start();
			return null;
		});
		((Mockery) stream2).method("send", Object.class).addResponse(args -> args[0] == message, args -> {
			synchronized (notary) {
				notary.notifyAll();
			}
			return null;
		});
		((Mockery) stream3).method("send", Object.class).addResponse(args -> args[0] == message, args -> null);
		((Mockery) returnStream).method("send", Object.class).queueResponse(args -> {
			assert args[0] instanceof AddressedMessageResult && ((AddressedMessageResult) args[0]).wasSuccessful();
			return null;
		});

		handler.handle(message, new NodeAddress(-1));

		try {
			Thread.sleep(3_000);
		} catch (InterruptedException e) {
		}

		((Mockery) returnStream).method("send", Object.class).assertQueueEmpty();
	}
	
	@Test
	public static void handlePayloadTest() {
		NetworkModel model = new NetworkModel();
		model.connect(new NodeAddress(1), new NodeAddress(2));
		model.connect(new NodeAddress(2), new NodeAddress(3));
		
		Map<NodeAddress, ObjectStream> connections = new HashMap<>();
		ObjectStream str1 = Testing.mock(ObjectStream.class);
		((Mockery) str1).method("send", Object.class).setResponse(MethodMocker.VOID);
		connections.put(new NodeAddress(2), str1);
		
		Map<NodeAddress, ChildNode> nodes = new HashMap<>();
		
		AddressedMessageHandler handler = new AddressedMessageHandler(new NodeAddress(1), model, connections, nodes);
		
		nodes.put(new NodeAddress(1), new ChildNode(handler, connections, new NodeAddress(1), new NodeAddress(1)));
		nodes.put(new NodeAddress(2), new ChildNode(handler, connections, new NodeAddress(1), new NodeAddress(2)));
		nodes.put(new NodeAddress(3), new ChildNode(handler, connections, new NodeAddress(1), new NodeAddress(3)));
		
		Object object = "hello world";
		AddressedPayload payload = new ClientTransmission(object);
		AddressedMessage message = new AddressedMessage(payload, new NodeAddress(3), new NodeAddress(1));
		
		handler.handle(message, new NodeAddress(2));
		
		try {
			assert nodes.get(new NodeAddress(3)).receive().equals("hello world");
		} catch (DisconnectionException e) {
			assert false;
		}
	}

}
