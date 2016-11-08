package test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

import com.phoenixkahlo.pnet.ProtocolViolationException;
import com.phoenixkahlo.pnet.serialization.Serializer;
import com.phoenixkahlo.ptest.Testing;

public class TestUtils {

	public static void testSerializer(Serializer serializer, Supplier<Object> supplier,
			BiPredicate<Object, Object> equalityTest) throws IOException, ProtocolViolationException {
		for (int n = 0; n < 20; n++) {
			Object obj1 = supplier.get();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			serializer.serialize(obj1, out);
			InputStream in = new ByteArrayInputStream(out.toByteArray());
			Object obj2 = serializer.toDeserializer().deserialize(in);
			if (!equalityTest.test(obj1, obj2)) {
				if (obj1 instanceof long[])
					System.out.println("test: " + Arrays.toString((long[]) obj1) + " ?= " + Arrays.toString((long[]) obj2));
				else if (obj1 instanceof int[])
					System.out.println("test: " + Arrays.toString((int[]) obj1) + " ?= " + Arrays.toString((int[]) obj2));
				else if (obj1 instanceof double[])
					System.out.println("test: " + Arrays.toString((double[]) obj1) + " ?= " + Arrays.toString((double[]) obj2));
				else if (obj1 instanceof float[])
					System.out.println("test: " + Arrays.toString((float[]) obj1) + " ?= " + Arrays.toString((float[]) obj2));
				else if (obj1 instanceof short[])
					System.out.println("test: " + Arrays.toString((short[]) obj1) + " ?= " + Arrays.toString((short[]) obj2));
				else if (obj1 instanceof char[])
					System.out.println("test: " + Arrays.toString((char[]) obj1) + " ?= " + Arrays.toString((char[]) obj2));
				else if (obj1 instanceof byte[])
					System.out.println("test: " + Arrays.toString((byte[]) obj1) + " ?= " + Arrays.toString((byte[]) obj2));
				else if (obj1 instanceof boolean[])
					System.out.println("test: " + Arrays.toString((boolean[]) obj1) + " ?= " + Arrays.toString((boolean[]) obj2));
				else if (obj1 instanceof Object[])
					System.out.println("test: " + Arrays.toString((Object[]) obj1) + " ?= " + Arrays.toString((Object[]) obj2));
				else
					System.out.println("test: " + obj1 + " ?= " + obj2);
				assert false;
			}
		}
	}

	public static void testSerializer(Serializer serializer, Supplier<Object> supplier)
			throws IOException, ProtocolViolationException {
		testSerializer(serializer, supplier, Object::equals);
	}

	public static String randomString() {
		StringBuilder builder = new StringBuilder();
		int len = Testing.RANDOM.nextInt(30);
		for (int i = 0; i < len; i++) {
			builder.append((char) (Testing.RANDOM.nextInt(94) + 32));
		}
		return builder.toString();
	}

}
