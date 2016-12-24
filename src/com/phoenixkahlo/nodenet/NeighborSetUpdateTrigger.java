package com.phoenixkahlo.nodenet;

import com.phoenixkahlo.nodenet.serialization.FieldSerializer;
import com.phoenixkahlo.nodenet.serialization.Serializer;

<<<<<<< HEAD
/**
 * Trigger to send NeighborSetUpdate. Descibed in package description.
 */
=======
>>>>>>> eb56286c0399094b26770a91c1ceb3d22c73ee44
public class NeighborSetUpdateTrigger implements ViralPayload {

	public static Serializer serializer(Serializer subSerializer) {
		return new FieldSerializer(NeighborSetUpdateTrigger.class, subSerializer, NeighborSetUpdateTrigger::new);
	}
<<<<<<< HEAD

=======
	
>>>>>>> eb56286c0399094b26770a91c1ceb3d22c73ee44
}
