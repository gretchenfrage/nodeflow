package com.phoenixkahlo.util;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import com.phoenixkahlo.nodenet.serialization.FieldSerializer;
import com.phoenixkahlo.nodenet.serialization.SerializationUtils;
import com.phoenixkahlo.nodenet.serialization.Serializer;

public class UUID {

	public static Serializer serializer() {
		return new FieldSerializer(UUID.class, UUID::new);
	}

	private long data1;
	private long data2;

	public UUID() {
		data1 = ThreadLocalRandom.current().nextLong();
		data2 = ThreadLocalRandom.current().nextLong();
	}

	@Deprecated
	public UUID(int n) {
		data1 = n;
	}
	
	public UUID(BigInteger n) {
		try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(n.toByteArray()))) {
			data1 = in.readLong();
			data2 = in.readLong();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public UUID(InputStream in) throws IOException {
		data1 = SerializationUtils.readLong(in);
		data2 = SerializationUtils.readLong(in);
	}
	
	public UUID(String str) throws IllegalArgumentException {
		this(new BigInteger(str, 36));
	}
	
	public void write(OutputStream out) throws IOException {
		SerializationUtils.writeLong(data1, out);
		SerializationUtils.writeLong(data2, out);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof UUID)
			return data1 == ((UUID) other).data1 && data2 == ((UUID) other).data2;
		else
			return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(data1, data2);
	}

	public BigInteger asBigInt() {
		return new BigInteger(SerializationUtils.concatenate(SerializationUtils.longToBytes(data1),
				SerializationUtils.longToBytes(data2)));
	}

	@Override
	public String toString() {
		return asBigInt().toString(36).toUpperCase();
	}

}
