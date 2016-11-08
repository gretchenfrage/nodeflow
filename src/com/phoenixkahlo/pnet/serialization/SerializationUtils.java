package com.phoenixkahlo.pnet.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import com.phoenixkahlo.pnet.ProtocolViolationException;

public class SerializationUtils {

	/**
	 * If type is primitive, serialize the raw primitive data. If the type is an
	 * enum, serialize the ordinal. Otherwise, delegate to the Serializer.
	 * Symmetrical to deserialize.
	 */
	public static void serialize(Object object, Class<?> type, Serializer serializer, OutputStream out)
			throws IOException {
		if (type == int.class)
			writeInt((Integer) object, out);
		else if (type == long.class)
			writeLong((Long) object, out);
		else if (type == double.class)
			writeDouble((Double) object, out);
		else if (type == float.class)
			writeFloat((Float) object, out);
		else if (type == short.class)
			writeShort((Short) object, out);
		else if (type == char.class)
			writeChar((Character) object, out);
		else if (type == byte.class)
			out.write((Byte) object);
		else if (type == boolean.class)
			writeBoolean((Boolean) object, out);
		else if (type.isEnum())
			writeInt(((Enum<?>) object).ordinal(), out);
		else
			serializer.serialize(object, out);
	}

	/**
	 * If type is primitive, deserialize the raw primitive data. If the type is
	 * an enum, find the constant by the deserialized ordinal. Otherwise,
	 * delegate to the Deserializer. Symmetrical to serialize.
	 */
	public static Object deserialize(Class<?> type, Deserializer deserializer, InputStream in)
			throws IOException, ProtocolViolationException {
		if (type == int.class)
			return readInt(in);
		else if (type == long.class)
			return readLong(in);
		else if (type == double.class)
			return readDouble(in);
		else if (type == float.class)
			return readFloat(in);
		else if (type == short.class)
			return readShort(in);
		else if (type == char.class)
			return readChar(in);
		else if (type == byte.class)
			return (byte) in.read();
		else if (type == boolean.class)
			return readBoolean(in);
		else if (type.isEnum())
			return type.getEnumConstants()[readInt(in)];
		else
			return deserializer.deserialize(in);
	}
	
	/**
	 * Serialize with UTF-8.
	 */
	public static byte[] stringToBytes(String string) {
		return string.getBytes(StandardCharsets.UTF_8);
	}
	
	/**
	 * Deserialize with UTF-8.
	 */
	public static String bytesToString(byte[] bytes) {
		return new String(bytes, StandardCharsets.UTF_8);
	}

	/**
	 * Head with array length, symmetrical to deserializeByteArray.
	 */
	public static void serializeByteArray(byte[] bytes, OutputStream out) throws IOException {
		writeInt(bytes.length, out);
		out.write(bytes);
	}
	
	/**
	 * Head with array length, symmetrical to serializeByteArray.
	 */
	public static byte[] deserializeByteArray(InputStream in) throws IOException {
		byte[] arr = new byte[readInt(in)];
		int read = 0;
		while (read < arr.length)
			read += in.read(arr, read, arr.length);
		return arr;
	}
	
	public static void writeInt(int n, OutputStream out) throws IOException {
		out.write(intToBytes(n));
	}

	public static int readInt(InputStream in) throws IOException {
		byte[] bytes = new byte[4];
		in.read(bytes);
		return bytesToInt(bytes);
	}

	public static void writeLong(long n, OutputStream out) throws IOException {
		out.write(longToBytes(n));
	}

	public static long readLong(InputStream in) throws IOException {
		byte[] bytes = new byte[8];
		in.read(bytes);
		return bytesToLong(bytes);
	}

	public static void writeDouble(double n, OutputStream out) throws IOException {
		out.write(doubleToBytes(n));
	}

	public static double readDouble(InputStream in) throws IOException {
		byte[] bytes = new byte[8];
		in.read(bytes);
		return bytesToDouble(bytes);
	}

	public static void writeFloat(float n, OutputStream out) throws IOException {
		out.write(floatToBytes(n));
	}

	public static float readFloat(InputStream in) throws IOException {
		byte[] bytes = new byte[4];
		in.read(bytes);
		return bytesToFloat(bytes);
	}

	public static void writeShort(short n, OutputStream out) throws IOException {
		out.write(shortToBytes(n));
	}

	public static short readShort(InputStream in) throws IOException {
		byte[] bytes = new byte[2];
		in.read(bytes);
		return bytesToShort(bytes);
	}

	public static void writeChar(char c, OutputStream out) throws IOException {
		out.write(charToBytes(c));
	}

	public static char readChar(InputStream in) throws IOException {
		byte[] bytes = new byte[2];
		in.read(bytes);
		return bytesToChar(bytes);
	}

	public static void writeBoolean(boolean b, OutputStream out) throws IOException {
		out.write(b ? 1 : 0);
	}

	public static boolean readBoolean(InputStream in) throws IOException {
		return in.read() != 0;
	}

	public static byte[] intToBytes(int n) {
		return ByteBuffer.allocate(4).putInt(n).array();
	}

	public static int bytesToInt(byte[] bytes) {
		return ByteBuffer.wrap(bytes).getInt();
	}

	public static byte[] longToBytes(long n) {
		return ByteBuffer.allocate(8).putLong(n).array();
	}

	public static long bytesToLong(byte[] bytes) {
		return ByteBuffer.wrap(bytes).getLong();
	}

	public static byte[] doubleToBytes(double n) {
		return ByteBuffer.allocate(8).putDouble(n).array();
	}

	public static double bytesToDouble(byte[] bytes) {
		return ByteBuffer.wrap(bytes).getDouble();
	}

	public static byte[] floatToBytes(float n) {
		return ByteBuffer.allocate(4).putFloat(n).array();
	}

	public static float bytesToFloat(byte[] bytes) {
		return ByteBuffer.wrap(bytes).getFloat();
	}

	public static byte[] shortToBytes(short n) {
		return ByteBuffer.allocate(2).putShort(n).array();
	}

	public static short bytesToShort(byte[] bytes) {
		return ByteBuffer.wrap(bytes).getShort();
	}

	public static byte[] charToBytes(char c) {
		return ByteBuffer.allocate(2).putChar(c).array();
	}

	public static char bytesToChar(byte[] bytes) {
		return ByteBuffer.wrap(bytes).getChar();
	}

}
