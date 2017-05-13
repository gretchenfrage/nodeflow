package com.phoenixkahlo.nodenet.serialization;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Phoenix on 5/12/2017.
 */
public class ArrayClassSerializer implements Serializer {

    private Serializer subSerializer;

    public ArrayClassSerializer(Serializer subSerializer) {
        this.subSerializer = subSerializer;
    }

    @Override
    public boolean canSerialize(Object object) {
        return object != null
                && object instanceof Class
                && ((Class<?>) object).isArray();
    }

    @Override
    public void serialize(Object object, OutputStream out) throws IOException {
        if (!canSerialize(object))
            throw new IllegalArgumentException("can't serialize: " + object);
        Class<?> itemClass = ((Class<?>) object).getComponentType();
        subSerializer.serialize(itemClass, out);
    }

    @Override
    public Deserializer toDeserializer() {
        return new ArrayClassDeserializer(subSerializer.toDeserializer());
    }
}
