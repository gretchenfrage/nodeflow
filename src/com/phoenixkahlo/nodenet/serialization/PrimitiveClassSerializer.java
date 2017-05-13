package com.phoenixkahlo.nodenet.serialization;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Phoenix on 5/12/2017.
 */
public class PrimitiveClassSerializer implements Serializer {

    private static StringSerializer stringSerializer = new StringSerializer();

    public static List<Class<?>> primitiveClasses = new ArrayList<>();
    static {
        primitiveClasses.add(int.class);
        primitiveClasses.add(long.class);
        primitiveClasses.add(byte.class);
        primitiveClasses.add(short.class);
        primitiveClasses.add(char.class);
        primitiveClasses.add(boolean.class);
        primitiveClasses.add(float.class);
        primitiveClasses.add(double.class);
    }

    @Override
    public boolean canSerialize(Object object) {
        return primitiveClasses.contains(object);
    }

    @Override
    public void serialize(Object object, OutputStream out) throws IOException {
        if (!canSerialize(object))
            throw new IllegalArgumentException("can't serialize: " + object);
        String name = ((Class<?>) object).getName();
        stringSerializer.serialize(name, out);
    }

    @Override
    public Deserializer toDeserializer() {
        return new PrimitiveClassDeserializer();
    }

}
