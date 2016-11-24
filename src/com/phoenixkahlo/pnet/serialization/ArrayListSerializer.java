package com.phoenixkahlo.pnet.serialization;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

@Deprecated
public class ArrayListSerializer implements Serializer {

	private Serializer subSerializer;

	public ArrayListSerializer(Serializer subSerializer) {
		this.subSerializer = subSerializer;
	}

	@Override
	public boolean canSerialize(Object object) {
		return object.getClass() == ArrayList.class;
	}

	@Override
	public void serialize(Object object, OutputStream out) throws IOException {
		if (!canSerialize(object))
			throw new IllegalArgumentException("cannot serialize " + object);
		List<?> list = (List<?>) object;
		SerializationUtils.writeInt(list.size(), out);
		for (Object item : list) {
			subSerializer.serialize(item, out);
		}
	}

	@Override
	public Deserializer toDeserializer() {
		return new ArrayListDeserializer(subSerializer.toDeserializer());
	}

}
