package test.serialization;

import java.util.Arrays;

import com.phoenixkahlo.pnet.serialization.ArraySerializer;
import com.phoenixkahlo.pnet.serialization.StringSerializer;
import com.phoenixkahlo.ptest.Test;
import com.phoenixkahlo.ptest.Testing;

public class ArraySerializationTest {

	@Test
	public static void serializeIntArr() throws Exception {
		TestUtils.testSerializer(new ArraySerializer(int.class), () -> {
			int[] arr = new int[Testing.RANDOM.nextInt(5000)];
			for (int i = 0; i < arr.length; i++) {
				arr[i] = Testing.RANDOM.nextInt();
			}
			return arr;
		}, (a, b) -> Arrays.equals((int[]) a, (int[]) b));
	}
	
	@Test
	public static void serializeLongArr() throws Exception {
		TestUtils.testSerializer(new ArraySerializer(long.class), () -> {
			long[] arr = new long[Testing.RANDOM.nextInt(5000)];
			for (int i = 0; i < arr.length; i++) {
				arr[i] = Testing.RANDOM.nextLong();
			}
			return arr;
		}, (a, b) -> Arrays.equals((long[]) a, (long[]) b));
	}
	
	@Test
	public static void serializeDoubleArr() throws Exception {
		TestUtils.testSerializer(new ArraySerializer(double.class), () -> {
			double[] arr = new double[Testing.RANDOM.nextInt(5000)];
			for (int i = 0; i < arr.length; i++) {
				arr[i] = Testing.RANDOM.nextDouble();
			}
			return arr;
		}, (a, b) -> Arrays.equals((double[]) a, (double[]) b));
	}

	@Test
	public static void serializeFloatArr() throws Exception {
		TestUtils.testSerializer(new ArraySerializer(float.class), () -> {
			float[] arr = new float[Testing.RANDOM.nextInt(5000)];
			for (int i = 0; i < arr.length; i++) {
				arr[i] = Testing.RANDOM.nextFloat();
			}
			return arr;
		}, (a, b) -> Arrays.equals((float[]) a, (float[]) b));
	}
	
	@Test
	public static void serializeShortArr() throws Exception {
		TestUtils.testSerializer(new ArraySerializer(short.class), () -> {
			short[] arr = new short[Testing.RANDOM.nextInt(5000)];
			for (int i = 0; i < arr.length; i++) {
				arr[i] = (short) Testing.RANDOM.nextInt();
			}
			return arr;
		}, (a, b) -> Arrays.equals((short[]) a, (short[]) b));
	}
	
	@Test
	public static void serializeCharArr() throws Exception {
		TestUtils.testSerializer(new ArraySerializer(char.class), () -> {
			char[] arr = new char[Testing.RANDOM.nextInt(5000)];
			for (int i = 0; i < arr.length; i++) {
				arr[i] = (char) Testing.RANDOM.nextInt();
			}
			return arr;
		}, (a, b) -> Arrays.equals((char[]) a, (char[]) b));
	}
	
	@Test
	public static void serializeByteArr() throws Exception {
		TestUtils.testSerializer(new ArraySerializer(byte.class), () -> {
			byte[] arr = new byte[Testing.RANDOM.nextInt(5000)];
			for (int i = 0; i < arr.length; i++) {
				arr[i] = (byte) Testing.RANDOM.nextInt();
			}
			return arr;
		}, (a, b) -> Arrays.equals((byte[]) a, (byte[]) b));
	}
	
	@Test
	public static void serializeBooleanArr() throws Exception {
		TestUtils.testSerializer(new ArraySerializer(boolean.class), () -> {
			boolean[] arr = new boolean[Testing.RANDOM.nextInt(5000)];
			for (int i = 0; i < arr.length; i++) {
				arr[i] = Testing.RANDOM.nextBoolean();
			}
			return arr;
		}, (a, b) -> Arrays.equals((boolean[]) a, (boolean[]) b));
	}
	
	@Test
	public static void serializeStringArray() throws Exception {
		TestUtils.testSerializer(new ArraySerializer(String.class, new StringSerializer()), () -> {
			String[] arr = new String[Testing.RANDOM.nextInt(5000)];
			for (int i = 0; i < arr.length; i++) {
				arr[i] = TestUtils.randomString();
			}
			return arr;
		}, (a, b) -> Arrays.equals((String[]) a, (String[]) b));
	}
	
}
