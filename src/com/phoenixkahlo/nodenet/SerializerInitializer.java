package com.phoenixkahlo.nodenet;

import java.util.ArrayList;
import java.util.HashSet;

import com.phoenixkahlo.nodenet.serialization.CollectionSerializer;
import com.phoenixkahlo.nodenet.serialization.UnionSerializer;
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
		serializer.add(-6, ViralPayload.serializer(serializer));
		serializer.add(-7, ConnectionNotification.serializer(serializer));
		serializer.add(-8, DisconnectionNotification.serializer(serializer));
		serializer.add(-9, AddressedPayloadResult.serializer(serializer));
	}
	
}
