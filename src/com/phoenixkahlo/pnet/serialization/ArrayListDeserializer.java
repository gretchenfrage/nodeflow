package com.phoenixkahlo.pnet.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.phoenixkahlo.pnet.ProtocolViolationException;

public class ArrayListDeserializer implements Deserializer {

	private Deserializer subDeserializer;
	
	public ArrayListDeserializer(Deserializer subDeserializer) {
		this.subDeserializer = subDeserializer;
	}
	
	@Override
	public Object deserialize(InputStream in) throws IOException, ProtocolViolationException {
		int length = SerializationUtils.readInt(in);
		if (length < 0)
			throw new ProtocolViolationException("ArrayList cannot have negative size");
		List<Object> list = new ArrayList<Object>();
		for (int i = 0; i < length; i++) {
			list.add(subDeserializer.deserialize(in));
		}
		return list;
	}

	@Override
	public Serializer toSerializer() {
		return new ArrayListSerializer(subDeserializer.toSerializer());
	}

}
