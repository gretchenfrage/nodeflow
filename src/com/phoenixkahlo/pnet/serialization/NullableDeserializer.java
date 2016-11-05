package com.phoenixkahlo.pnet.serialization;

import java.io.IOException;
import java.io.InputStream;

import com.phoenixkahlo.pnet.ProtocolViolationException;

public class NullableDeserializer implements Deserializer {

	private Deserializer subDeserializer;
	
	public NullableDeserializer(Deserializer subDeserializer) {
		this.subDeserializer = subDeserializer;
	}

	@Override
	public Object deserialize(InputStream in) throws IOException, ProtocolViolationException {
		if (SerializationUtils.readBoolean(in))
			return null;
		else
			return subDeserializer.deserialize(in);
	}

	@Override
	public Serializer toSerializer() {
		return new NullableSerializer(subDeserializer.toSerializer());
	}

}
