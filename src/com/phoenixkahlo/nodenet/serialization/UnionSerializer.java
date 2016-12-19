package com.phoenixkahlo.pnet.serialization;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class UnionSerializer implements Serializer {

	private Map<Serializer, Integer> subSerializers = new HashMap<Serializer, Integer>();
	/**
	 * UnionSerializer is cached upon invocations of toDeserializer, and built
	 * from subSerializers, before being returned. This is because
	 * subSerializers are expected to invoke this.toDeserializer in a recursive
	 * fashion. toDeserializer should be nullified upon modification of the
	 * serializer.
	 */
	private UnionDeserializer toDeserializer;

	public void add(int header, Serializer serializer) {
		subSerializers.put(serializer, header);
		toDeserializer = null;
	}

	@Override
	public boolean canSerialize(Object object) {
		return subSerializers.keySet().stream().anyMatch(sub -> sub.canSerialize(object));
	}

	@Override
	public void serialize(Object object, OutputStream out) throws IOException {
		if (!canSerialize(object))
			throw new IllegalArgumentException("cannot serialize " + object);
		Serializer serializer = subSerializers.keySet().stream().filter(sub -> sub.canSerialize(object)).findAny()
				.get();
		SerializationUtils.writeInt(subSerializers.get(serializer), out);
		serializer.serialize(object, out);
	}

	@Override
	public Deserializer toDeserializer() {
		if (toDeserializer == null) {
			toDeserializer = new UnionDeserializer();
			for (Map.Entry<Serializer, Integer> entry : subSerializers.entrySet()) {
				toDeserializer.add(entry.getValue(), entry.getKey().toDeserializer());
			}
		}
		return toDeserializer;
	}

}
