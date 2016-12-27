package com.phoenixkahlo.nodenet;

import com.phoenixkahlo.nodenet.serialization.FieldSerializer;
import com.phoenixkahlo.nodenet.serialization.Serializer;

/**
 * Trigger to send NeighborSetUpdate. Descibed in package description.
 */
public class NeighborSetUpdateTrigger implements ViralPayload {

	public static Serializer serializer(Serializer subSerializer) {
		return new FieldSerializer(NeighborSetUpdateTrigger.class, subSerializer, NeighborSetUpdateTrigger::new);
	}
	
	@Override
	public String toString() {
		return "neighbor set update trigger";
	}
	
}
