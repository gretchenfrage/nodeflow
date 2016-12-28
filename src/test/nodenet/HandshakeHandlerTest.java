package test.nodenet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import com.phoenixkahlo.nodenet.AddressedMessageHandler;
import com.phoenixkahlo.nodenet.ChildNode;
import com.phoenixkahlo.nodenet.Handshake;
import com.phoenixkahlo.nodenet.HandshakeHandler;
import com.phoenixkahlo.nodenet.LeaveJoinHandler;
import com.phoenixkahlo.nodenet.NetworkModel;
import com.phoenixkahlo.nodenet.Node;
import com.phoenixkahlo.nodenet.NodeAddress;
import com.phoenixkahlo.nodenet.ProtocolViolationException;
import com.phoenixkahlo.nodenet.ViralMessageHandler;
import com.phoenixkahlo.nodenet.serialization.Deserializer;
import com.phoenixkahlo.nodenet.serialization.SerializationUtils;
import com.phoenixkahlo.nodenet.serialization.Serializer;
import com.phoenixkahlo.nodenet.stream.DatagramStream;
import com.phoenixkahlo.nodenet.stream.ObjectStream;
import com.phoenixkahlo.ptest.MethodMocker;
import com.phoenixkahlo.ptest.Mockery;
import com.phoenixkahlo.ptest.Test;
import com.phoenixkahlo.ptest.Testing;

public class HandshakeHandlerTest {

	public static void main(String[] args) {
		test1();
	}

	private static Map<Integer, Object> fakeSerialized = Collections.synchronizedMap(new HashMap<>());

	private static class FakeSerializer implements Serializer {

		@Override
		public boolean canSerialize(Object object) {
			return true;
		}

		@Override
		public void serialize(Object object, OutputStream out) throws IOException {
			int id = ThreadLocalRandom.current().nextInt();
			fakeSerialized.put(id, object);
			SerializationUtils.writeInt(id, out);
		}

		@Override
		public Deserializer toDeserializer() {
			return new FakeDeserializer();
		}

	};

	private static class FakeDeserializer implements Deserializer {

		@Override
		public Object deserialize(InputStream in) throws IOException, ProtocolViolationException {
			int id = SerializationUtils.readInt(in);
			return Optional.ofNullable(fakeSerialized.get(id)).get();
		}

		@Override
		public Serializer toSerializer() {
			return new FakeSerializer();
		}

	}

	@Test
	public static void test1() {
		// situtation 1: everything goes normally
		Serializer serializer = new FakeSerializer();

		NodeAddress localAddress = new NodeAddress(1);

		NetworkModel model = new NetworkModel();
		model.connect(new NodeAddress(1), new NodeAddress(2));
		model.connect(new NodeAddress(1), new NodeAddress(3));

		Map<NodeAddress, ObjectStream> connections = new HashMap<>();
		connections.put(new NodeAddress(2), Testing.mock(ObjectStream.class));
		connections.put(new NodeAddress(3), Testing.mock(ObjectStream.class));

		Map<NodeAddress, ChildNode> nodes = new HashMap<>();

		AddressedMessageHandler addressedHandler = new AddressedMessageHandler(localAddress, model, connections, nodes, System.err);
		LeaveJoinHandler leaveJoinHandler = new LeaveJoinHandler(new NodeAddress(1), model, nodes, address -> new ChildNode(null, connections, new NodeAddress(1), address));
		ViralMessageHandler viralHandler = new ViralMessageHandler(localAddress, connections, leaveJoinHandler, System.err);

		nodes.put(new NodeAddress(1), new ChildNode(addressedHandler, connections, localAddress, new NodeAddress(1)));
		nodes.put(new NodeAddress(2), new ChildNode(addressedHandler, connections, localAddress, new NodeAddress(2)));
		nodes.put(new NodeAddress(3), new ChildNode(addressedHandler, connections, localAddress, new NodeAddress(3)));

		HandshakeHandler handshakeHandler = new HandshakeHandler(serializer, localAddress, connections, nodes,
				viralHandler, addressedHandler, leaveJoinHandler, System.err);

		DatagramStream connector = Testing.mock(DatagramStream.class);
		((Mockery) connector).method("send", byte[].class).setResponse(MethodMocker.VOID);
		for (ObjectStream connection : connections.values()) {
			((Mockery) connection).method("send", Object.class).setResponse(MethodMocker.VOID);
		}

		((Mockery) connector).method("receive").queueResponse(args -> {
			Handshake handshake = new Handshake(new NodeAddress(4));
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				serializer.serialize(handshake, baos);
			} catch (IOException e) {
				System.err.println("oh no...");
				e.printStackTrace();
			}
			return baos.toByteArray();
		});

		Object notary = new Object();

		((Mockery) connector).method("receive").queueResponse(args -> {
			try {
				synchronized (notary) {
					notary.wait();
				}
			} catch (InterruptedException e) {
			}
			Thread.currentThread().stop();
			return null;
		});
		((Mockery) connector).method("setDisconnectHandler", Runnable.class, boolean.class).setResponse(MethodMocker.VOID);
		

		Optional<Node> node = handshakeHandler.setup(connector);
		
		assert model.connected(new NodeAddress(1), new NodeAddress(4));
		assert connections.containsKey(new NodeAddress(4));
		assert nodes.containsKey(new NodeAddress(4));

		synchronized (notary) {
			notary.notifyAll();
		}
	}

}
