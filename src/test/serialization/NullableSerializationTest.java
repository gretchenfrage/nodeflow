package test.serialization;

import java.io.IOException;

import com.phoenixkahlo.nodenet.ProtocolViolationException;
import com.phoenixkahlo.nodenet.serialization.NullableSerializer;
import com.phoenixkahlo.nodenet.serialization.StringSerializer;
import com.phoenixkahlo.ptest.Test;
import com.phoenixkahlo.ptest.Testing;

public class NullableSerializationTest {

	@Test
	public static void testNullableSerialization() throws IOException, ProtocolViolationException {
		TestUtils.testSerializer(new NullableSerializer(new StringSerializer()), () -> {
			if (Testing.RANDOM.nextBoolean())
				return TestUtils.randomString();
			else
				return null;
		}, (a, b) -> a == b || a.equals(b));
	}
	
}
