package test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Random;

import com.phoenixkahlo.pnet.serialization.ArraySerializer;
import com.phoenixkahlo.pnet.serialization.Deserializer;
import com.phoenixkahlo.pnet.serialization.Serializer;
import com.phoenixkahlo.ptest.Test;

public class ArraySerializationTest {

	@Test
	public static void testArraySerializerInt() throws Exception {
		Serializer serializer = new ArraySerializer(int.class, null);
		Deserializer deserializer = serializer.toDeserializer();
		
		Random random = new Random();
		for (int n = 0; n < 50; n++) {
			int[] arr = new int[random.nextInt(1000)];
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			serializer.serialize(arr, out);
			InputStream in = new ByteArrayInputStream(out.toByteArray());
			int[] arr2 = (int[]) deserializer.deserialize(in);
			assert Arrays.equals(arr, arr2);
		}
	}
	
	@Test
	public static void testArraySerializerLong() throws Exception {
		Serializer serializer = new ArraySerializer(long.class, null);
		Deserializer deserializer = serializer.toDeserializer();
		
		Random random = new Random();
		for (int n = 0; n < 50; n++) {
			long[] arr = new long[random.nextInt(1000)];
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			serializer.serialize(arr, out);
			InputStream in = new ByteArrayInputStream(out.toByteArray());
			long[] arr2 = (long[]) deserializer.deserialize(in);
			assert Arrays.equals(arr, arr2);
		}
	}
	
	@Test
	public static void testArraySerializerDouble() throws Exception {
		Serializer serializer = new ArraySerializer(double.class, null);
		Deserializer deserializer = serializer.toDeserializer();
		
		Random random = new Random();
		for (int n = 0; n < 50; n++) {
			double[] arr = new double[random.nextInt(1000)];
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			serializer.serialize(arr, out);
			InputStream in = new ByteArrayInputStream(out.toByteArray());
			double[] arr2 = (double[]) deserializer.deserialize(in);
			assert Arrays.equals(arr, arr2);
		}
	}
	
	@Test
	public static void testArraySerializerFloat() throws Exception {
		Serializer serializer = new ArraySerializer(float.class, null);
		Deserializer deserializer = serializer.toDeserializer();
		
		Random random = new Random();
		for (int n = 0; n < 50; n++) {
			float[] arr = new float[random.nextInt(1000)];
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			serializer.serialize(arr, out);
			InputStream in = new ByteArrayInputStream(out.toByteArray());
			float[] arr2 = (float[]) deserializer.deserialize(in);
			assert Arrays.equals(arr, arr2);
		}
	}
	
	@Test
	public static void testArraySerializerShort() throws Exception {
		Serializer serializer = new ArraySerializer(short.class, null);
		Deserializer deserializer = serializer.toDeserializer();
		
		Random random = new Random();
		for (int n = 0; n < 50; n++) {
			short[] arr = new short[random.nextInt(1000)];
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			serializer.serialize(arr, out);
			InputStream in = new ByteArrayInputStream(out.toByteArray());
			short[] arr2 = (short[]) deserializer.deserialize(in);
			assert Arrays.equals(arr, arr2);
		}
	}
	
	@Test
	public static void testArraySerializerChar() throws Exception {
		Serializer serializer = new ArraySerializer(char.class, null);
		Deserializer deserializer = serializer.toDeserializer();
		
		Random random = new Random();
		for (int n = 0; n < 50; n++) {
			char[] arr = new char[random.nextInt(1000)];
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			serializer.serialize(arr, out);
			InputStream in = new ByteArrayInputStream(out.toByteArray());
			char[] arr2 = (char[]) deserializer.deserialize(in);
			assert Arrays.equals(arr, arr2);
		}
	}
	
	@Test
	public static void testArraySerializerByte() throws Exception {
		Serializer serializer = new ArraySerializer(byte.class, null);
		Deserializer deserializer = serializer.toDeserializer();
		
		Random random = new Random();
		for (int n = 0; n < 50; n++) {
			byte[] arr = new byte[random.nextInt(1000)];
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			serializer.serialize(arr, out);
			InputStream in = new ByteArrayInputStream(out.toByteArray());
			byte[] arr2 = (byte[]) deserializer.deserialize(in);
			assert Arrays.equals(arr, arr2);
		}
	}
	
	@Test
	public static void testArraySerializerBoolean() throws Exception {
		Serializer serializer = new ArraySerializer(boolean.class, null);
		Deserializer deserializer = serializer.toDeserializer();
		
		Random random = new Random();
		for (int n = 0; n < 50; n++) {
			boolean[] arr = new boolean[random.nextInt(1000)];
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			serializer.serialize(arr, out);
			InputStream in = new ByteArrayInputStream(out.toByteArray());
			boolean[] arr2 = (boolean[]) deserializer.deserialize(in);
			assert Arrays.equals(arr, arr2);
		}
	}
	
}
