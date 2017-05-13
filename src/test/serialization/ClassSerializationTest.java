package test.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.phoenixkahlo.nodenet.ProtocolViolationException;
import com.phoenixkahlo.nodenet.serialization.ClassDeserializer;
import com.phoenixkahlo.nodenet.serialization.ClassSerializer;
import com.phoenixkahlo.nodenet.serialization.Deserializer;
import com.phoenixkahlo.nodenet.serialization.Serializer;
import com.phoenixkahlo.ptest.Test;

public class ClassSerializationTest {

	public static void main(String[] args) throws IOException, ProtocolViolationException {
		arrayListClassTest();
	}
	
	@Test
	public static void arrayListClassTest() throws IOException, ProtocolViolationException {
		Serializer serializer = new ClassSerializer();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		serializer.serialize(ArrayList.class, baos);
		Deserializer deserializer = new ClassDeserializer();
		Object deserialized = deserializer.deserialize(new ByteArrayInputStream(baos.toByteArray()));
		assert deserialized.equals(ArrayList.class);
	}

	@Test
	public static void intClassTest() throws IOException, ProtocolViolationException {
		
	}
	
}
