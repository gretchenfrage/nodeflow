package com.phoenixkahlo.nodenet.serialization;
import java.io.IOException;
import java.io.OutputStream;

public class NullableSerializer implements Serializer {

	private Serializer subSerializer;
	
	public NullableSerializer(Serializer subSerializer) {
		this.subSerializer = subSerializer;
	}

	@Override
	public boolean canSerialize(Object object) {
		return object == null || subSerializer.canSerialize(object);
	}

	@Override
	public void serialize(Object object, OutputStream out) throws IOException {
		SerializationUtils.writeBoolean(object == null, out);
		if (object != null) {
			subSerializer.serialize(object, out);
		}
	}

	@Override
	public Deserializer toDeserializer() {
		return new NullableDeserializer(subSerializer.toDeserializer());
	}

}
