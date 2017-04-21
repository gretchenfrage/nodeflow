package com.phoenixkahlo.nodenet.smartserial;

import java.util.Map;

/**
 * Placeholder for a byte sequences, that will be provided the reference addresses and then asked to produce a sequence.
 */
public interface ByteSequenceFormer {

    void reifyReferences(Map<Object, byte[]> referenceAddresses);

    byte[] produce();

    int size();

    /**
     * @return whether the two objects will produce the same byte sequence
     */
    @Override
    boolean equals(Object other);

}
