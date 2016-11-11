package test;

import com.phoenixkahlo.ptest.Testing;

public class MasterTest {

	public static void main(String[] args) {
		Testing.test(ArraySerializationTest.class);
		Testing.test(StringSerializationTest.class);
		Testing.test(ArrayListSerializationTest.class);
		Testing.test(HashMapSerializationTest.class);
		Testing.test(NullableSerializationTest.class);
		Testing.test(FieldSerializerTest.class);
		Testing.test(UnionSerializerTest.class);
		Testing.test(ArraySplitTest.class);
	}
	
}
