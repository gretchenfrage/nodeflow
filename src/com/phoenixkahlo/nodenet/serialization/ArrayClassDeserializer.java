package com.phoenixkahlo.nodenet.serialization;

import com.phoenixkahlo.nodenet.ProtocolViolationException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;

/**
 * Created by Phoenix on 5/12/2017.
 */
public class ArrayClassDeserializer implements Deserializer {

    private Deserializer subDeserializer;

    public ArrayClassDeserializer(Deserializer subDeserializer) {
        this.subDeserializer = subDeserializer;
    }

    @Override
    public Object deserialize(InputStream in) throws IOException, ProtocolViolationException {
        Object itemClass = subDeserializer.deserialize(in);
        if (!(itemClass instanceof Class))
            throw new ProtocolViolationException("expected class but received " + itemClass);
        return Array.newInstance((Class<?>) itemClass, 0).getClass();
    }

    @Override
    public Serializer toSerializer() {
        return null;
    }
}
