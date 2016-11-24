package test.serialization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.phoenixkahlo.pnet.ProtocolViolationException;
import com.phoenixkahlo.pnet.serialization.CollectionSerializer;
import com.phoenixkahlo.pnet.serialization.StringSerializer;
import com.phoenixkahlo.ptest.Test;
import com.phoenixkahlo.ptest.Testing;

public class CollectionSerializationTest {

	@Test
	public static void arrayList() throws IOException, ProtocolViolationException {
		TestUtils.testSerializer(new CollectionSerializer<>(ArrayList.class, ArrayList::new, new StringSerializer()), () -> {
			List<String> strings = new ArrayList<String>();
			for (int i = 0; i < Testing.RANDOM.nextInt(5000); i++) {
				strings.add(TestUtils.randomString());
			}
			return strings;
		});
	}
	
	@Test
	public static void hashSet() throws IOException, ProtocolViolationException {
		TestUtils.testSerializer(new CollectionSerializer<>(HashSet.class, HashSet::new, new StringSerializer()), () -> {
			Set<String> strings = new HashSet<String>();
			for (int i = 0; i < Testing.RANDOM.nextInt(5000); i++) {
				strings.add(TestUtils.randomString());
			}
			return strings;
		});
	}
	
	@Test
	public static void linkedList() throws IOException, ProtocolViolationException {
		TestUtils.testSerializer(new CollectionSerializer<>(LinkedList.class, LinkedList::new, new StringSerializer()), () -> {
			List<String> strings = new LinkedList<String>();
			for (int i = 0; i < Testing.RANDOM.nextInt(5000); i++) {
				strings.add(TestUtils.randomString());
			}
			return strings;
		});
	}
	
}
