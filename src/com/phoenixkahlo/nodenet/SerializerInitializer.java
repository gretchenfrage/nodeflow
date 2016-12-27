package com.phoenixkahlo.nodenet;

import java.util.ArrayList;
import java.util.HashSet;

import com.phoenixkahlo.nodenet.serialization.CollectionSerializer;
import com.phoenixkahlo.nodenet.serialization.NullSerializer;
import com.phoenixkahlo.nodenet.serialization.UnionSerializer;
import com.phoenixkahlo.util.UnorderedTuple;

/**
 * Static utility to initializer a UnionSerializer.
 */
public class SerializerInitializer {

	private SerializerInitializer() {
	}

	public static void init(UnionSerializer serializer) {
		serializer.add(0, new NullSerializer());
		serializer.add(-1, UnorderedTuple.serializer(serializer));
		serializer.add(-2, NodeAddress.serializer(serializer));
		serializer.add(-3, new CollectionSerializer<>(HashSet.class, HashSet::new, serializer));
		serializer.add(-4, new CollectionSerializer<>(ArrayList.class, ArrayList::new, serializer));
		serializer.add(-5, Handshake.serializer(serializer));
		serializer.add(-6, ViralMessage.serializer(serializer));
		serializer.add(-9, AddressedMessageResult.serializer(serializer));
		serializer.add(-10, AddressedMessage.serializer(serializer));
		serializer.add(-11, NeighborSetUpdate.serializer(serializer));
		serializer.add(-12, NeighborSetUpdateTrigger.serializer(serializer));
		serializer.add(-13, ClientTransmission.serializer(serializer));
	}

}
