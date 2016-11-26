package com.phoenixkahlo.pnet;

import java.util.ArrayList;
import java.util.HashSet;

import com.phoenixkahlo.pnet.serialization.CollectionSerializer;
import com.phoenixkahlo.pnet.serialization.UnionSerializer;
import com.phoenixkahlo.util.UnorderedTuple;

public class SerializerInitializer {

	private SerializerInitializer() {
	}

	public static void init(UnionSerializer serializer) {
		serializer.add(-1, UnorderedTuple.serializer(serializer));
		serializer.add(-2, NodeAddress.serializer(serializer));
		serializer.add(-3, new CollectionSerializer<>(HashSet.class, HashSet::new, serializer));
		serializer.add(-4, new CollectionSerializer<>(ArrayList.class, ArrayList::new, serializer));
		serializer.add(-5, Handshake.serializer(serializer));
	}
	
}
