package test.serialization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.phoenixkahlo.nodenet.ProtocolViolationException;
import com.phoenixkahlo.nodenet.serialization.ArraySerializer;
import com.phoenixkahlo.nodenet.serialization.CollectionSerializer;
import com.phoenixkahlo.nodenet.serialization.FieldSerializer;
import com.phoenixkahlo.nodenet.serialization.HashMapSerializer;
import com.phoenixkahlo.nodenet.serialization.StringSerializer;
import com.phoenixkahlo.nodenet.serialization.UnionSerializer;
import com.phoenixkahlo.ptest.Test;
import com.phoenixkahlo.ptest.Testing;

public class UnionSerializerTest {

	@Test
	public static void test1() throws IOException, ProtocolViolationException {
		UnionSerializer union = new UnionSerializer();
		union.add(1, new StringSerializer());
		union.add(2, new CollectionSerializer<>(ArrayList.class, ArrayList::new, union));
		union.add(3, new HashMapSerializer(union));
		union.add(4, new ArraySerializer(Object.class, union));
		union.add(6, new FieldSerializer(FieldSerializerTest.TestClass2.class, union, FieldSerializerTest.TestClass2::new));
		union.add(5, new FieldSerializer(FieldSerializerTest.TestClass1.class, union, FieldSerializerTest.TestClass1::new));
		TestUtils.testSerializer(union, () -> {
			if (Testing.RANDOM.nextInt(5) == 0) {
				return TestUtils.randomString();
			} else if (Testing.RANDOM.nextInt(4) == 0) {
				List<String> list = new ArrayList<String>();
				for (int i = 0; i < Testing.RANDOM.nextInt(20); i++) {
					list.add(TestUtils.randomString());
				}
				return list;
			} else if (Testing.RANDOM.nextInt(3) == 0) {
				Map<String, String> map = new HashMap<>();
				for (int i = 0; i < Testing.RANDOM.nextInt(20); i++) {
					map.put(TestUtils.randomString(), TestUtils.randomString());
				}
				return map;
			} else if (Testing.RANDOM.nextBoolean()) {
				Object[] arr = new Object[Testing.RANDOM.nextInt(100)];
				for (int i = 0; i < arr.length; i++) {
					arr[i] = TestUtils.randomString();
				}
				return arr;
			} else {
				return new FieldSerializerTest.TestClass2();
			}
		}, (a, b) -> {
			if (a instanceof Object[])
				return Arrays.equals((Object[]) a, (Object[]) b);
			else
				return a.equals(b);
		});
	}

}
