package com.phoenixkahlo.nodenet.smartserial;

import java.util.List;
import java.util.Set;

/**
 * One type use serializer for a particular object supplied by an ObjectSerializerSupplier.
 */
public interface ObjectSerializer {

    Set<Object> getConnected();

    List<ByteSequenceFormer> getSequence();

    ByteSequenceFormer getID();

}
