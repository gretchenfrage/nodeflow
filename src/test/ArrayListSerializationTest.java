package test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.phoenixkahlo.pnet.ProtocolViolationException;
import com.phoenixkahlo.pnet.serialization.ArrayListSerializer;
import com.phoenixkahlo.pnet.serialization.StringSerializer;
import com.phoenixkahlo.ptest.Test;
import com.phoenixkahlo.ptest.Testing;

public class ArrayListSerializationTest {

	@Test
	public static void testStringList() throws IOException, ProtocolViolationException {
		TestUtils.testSerializer(new ArrayListSerializer(new StringSerializer()), () -> {
			List<String> strings = new ArrayList<String>();
			for (int i = 0; i < Testing.RANDOM.nextInt(5000); i++) {
				strings.add(TestUtils.randomString());
			}
			return strings;
		});
	}
	
}
