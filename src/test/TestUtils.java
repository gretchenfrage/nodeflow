package test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

import com.phoenixkahlo.pnet.ProtocolViolationException;
import com.phoenixkahlo.pnet.serialization.Serializer;
import com.phoenixkahlo.ptest.Testing;

public class TestUtils {

	public static void testSerializer(Serializer serializer, Supplier<Object> supplier,
			BiPredicate<Object, Object> equalityTest) throws IOException, ProtocolViolationException {
		for (int n = 0; n < 100; n++) {
			Object obj1 = supplier.get();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			serializer.serialize(obj1, out);
			InputStream in = new ByteArrayInputStream(out.toByteArray());
			Object obj2 = serializer.toDeserializer().deserialize(in);
			assert equalityTest.test(obj1, obj2);
		}
	}
	
	public static void testSerializer(Serializer serializer, Supplier<Object> supplier) throws IOException, ProtocolViolationException {
		testSerializer(serializer, supplier, Object::equals);
	}
	
	public static String randomString() {
		StringBuilder builder = new StringBuilder();
		int len = Testing.RANDOM.nextInt(100);
		for (int i = 0; i < len; i++) {
			builder.append((char) Testing.RANDOM.nextInt());
		}
		return builder.toString();
	}
	
}
