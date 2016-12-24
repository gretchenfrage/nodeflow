package com.phoenixkahlo.nodenet;

import java.util.ArrayList;
import java.util.HashSet;

import com.phoenixkahlo.nodenet.serialization.CollectionSerializer;
import com.phoenixkahlo.nodenet.serialization.UnionSerializer;
import com.phoenixkahlo.util.UnorderedTuple;

<<<<<<< HEAD
/**
 * Static utility to initializer a UnionSerializer.
 */
=======
>>>>>>> eb56286c0399094b26770a91c1ceb3d22c73ee44
public class SerializerInitializer {

	private SerializerInitializer() {
	}

	public static void init(UnionSerializer serializer) {
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
	}
<<<<<<< HEAD

=======
	
>>>>>>> eb56286c0399094b26770a91c1ceb3d22c73ee44
}
