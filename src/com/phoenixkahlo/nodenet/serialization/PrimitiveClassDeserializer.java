package com.phoenixkahlo.nodenet.serialization;

import com.phoenixkahlo.nodenet.ProtocolViolationException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Phoenix on 5/12/2017.
 */
public class PrimitiveClassDeserializer implements Deserializer {

    private static StringDeserializer stringDeserializer = new StringDeserializer();

    @Override
    public Object deserialize(InputStream in) throws IOException, ProtocolViolationException {
        String name = (String) stringDeserializer.deserialize(in);
        switch (name) {
            case "int":
                return int.class;
            case "long":
                return long.class;
            case "float":
                return float.class;
            case "double":
                return double.class;
            case "short":
                return short.class;
            case "byte":
                return byte.class;
            case "char":
                return char.class;
            case "boolean":
                return boolean.class;
            default:
                throw new ProtocolViolationException();
        }
    }

    @Override
    public Serializer toSerializer() {
        return new PrimitiveClassSerializer();
    }

}
