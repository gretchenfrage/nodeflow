package com.phoenixkahlo.nodenet.smartserial;

/**
 * Supplies ObjectSerializers.
 */
public interface ObjectSerializerSupplier {

    ObjectSerializer getSerializer(Object object);

}
