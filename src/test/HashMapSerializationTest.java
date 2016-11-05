package test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.phoenixkahlo.pnet.ProtocolViolationException;
import com.phoenixkahlo.pnet.serialization.HashMapSerializer;
import com.phoenixkahlo.pnet.serialization.StringSerializer;
import com.phoenixkahlo.ptest.Test;
import com.phoenixkahlo.ptest.Testing;

public class HashMapSerializationTest {

	@Test
	public static void testHashMapSerialization() throws IOException, ProtocolViolationException {
		TestUtils.testSerializer(new HashMapSerializer(new StringSerializer()), () -> {
			Map<String, String> map = new HashMap<>();
			for (int i = 0; i < Testing.RANDOM.nextInt(5000); i++) {
				map.put(TestUtils.randomString(), TestUtils.randomString());
			}
			return map;
		});
	}
	
}
