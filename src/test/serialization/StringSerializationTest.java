package test.serialization;

import com.phoenixkahlo.nodenet.serialization.StringSerializer;
import com.phoenixkahlo.ptest.Test;

public class StringSerializationTest {

	@Test
	public static void test() throws Exception {
		TestUtils.testSerializer(new StringSerializer(), TestUtils::randomString);
	}
	
}
