package test;

import java.io.IOException;

import com.phoenixkahlo.pnet.ProtocolViolationException;
import com.phoenixkahlo.pnet.serialization.FieldSerializer;
import com.phoenixkahlo.ptest.Test;
import com.phoenixkahlo.ptest.Testing;

public class FieldSerializerTest {

	public static class TestClass1 {
		private int a = Testing.RANDOM.nextInt();
		private double b = Testing.RANDOM.nextDouble();

		@Override
		public boolean equals(Object other) {
			if (other instanceof TestClass1)
				return ((TestClass1) other).a == a && ((TestClass1) other).b == b;
			else
				return false;
		}
	}

	@Test
	public static void test1() throws IOException, ProtocolViolationException {
		TestUtils.testSerializer(new FieldSerializer(TestClass1.class, TestClass1::new), TestClass1::new);
	}

	public static class TestClass2 {
		private boolean a = Testing.RANDOM.nextBoolean();
		private TestClass1 sub = new TestClass1();

		@Override
		public boolean equals(Object other) {
			if (other instanceof TestClass2)
				return ((TestClass2) other).a == a && ((TestClass2) other).sub.equals(sub);
			else
				return false;
		}
	}

	@Test
	public static void test2() throws Exception {
		TestUtils.testSerializer(new FieldSerializer(TestClass2.class,
				new FieldSerializer(TestClass1.class, TestClass1::new), TestClass2::new), TestClass2::new);
	}

	public static class TransientClass {
		private transient int a = 0;
		private transient double b = 0;
	}

	@Test
	public static void transientTest() throws Exception {
		TestUtils.testSerializer(new FieldSerializer(TransientClass.class, TransientClass::new), () -> {
			TransientClass trans = new TransientClass();
			trans.a = Testing.RANDOM.nextInt();
			trans.b = Testing.RANDOM.nextDouble();
			return trans;
		}, (a, b) -> ((TransientClass) b).a == 0 && ((TransientClass) b).b == 0);
	}

}
