package test;

import com.phoenixkahlo.ptest.Testing;

import test.nodenet.AddressedAttemptSequenceTest;
import test.nodenet.AddressedMessageHandlerTest;
import test.nodenet.HandshakeHandlerTest;
import test.nodenet.NetworkModelTest;
import test.nodenet.ViralMessageHandlerTest;
import test.serialization.ArraySerializationTest;
import test.serialization.ArraySplitTest;
import test.serialization.CollectionSerializationTest;
import test.serialization.FieldSerializerTest;
import test.serialization.HashMapSerializationTest;
import test.serialization.NullableSerializationTest;
import test.serialization.StringSerializationTest;
import test.serialization.UnionSerializerTest;
import test.socket.BasicChildSocketTest;
import test.socket.RealUDPSocketWrapperTest;
import test.util.BlockingHashMapTest;

public class MasterTest {

	public static void main(String[] args) {
		Testing.test(ArraySerializationTest.class);
		Testing.test(StringSerializationTest.class);
		Testing.test(HashMapSerializationTest.class);
		Testing.test(NullableSerializationTest.class);
		Testing.test(FieldSerializerTest.class);
		Testing.test(UnionSerializerTest.class);
		Testing.test(ArraySplitTest.class);
		Testing.test(CollectionSerializationTest.class);
		
		Testing.test(RealUDPSocketWrapperTest.class);
		Testing.test(BasicChildSocketTest.class);
		
		Testing.test(NetworkModelTest.class);
		Testing.test(AddressedAttemptSequenceTest.class);
		Testing.test(AddressedMessageHandlerTest.class);
		Testing.test(ViralMessageHandlerTest.class);
		Testing.test(HandshakeHandlerTest.class);
		
		Testing.test(BlockingHashMapTest.class);
		
		System.out.println("$$$ done! $$$);
	}
	
}
