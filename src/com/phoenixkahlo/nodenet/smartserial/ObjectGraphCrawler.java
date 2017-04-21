package com.phoenixkahlo.nodenet.smartserial;

import java.util.*;

/**
 * Crawls an object graph to produce a collection of ObjectSerializers.
 */
public class ObjectGraphCrawler {

    public static List<ObjectSerializer> crawl(Object start, ObjectSerializerSupplier serializerSupplier) {
        Map<Object, ObjectSerializer> collection = new HashMap<>();

        Stack<Object> toCrawl = new Stack<>();
        toCrawl.add(start);

        while (toCrawl.size() > 0) {
            Object object = toCrawl.pop();
            if (!collection.containsKey(object)) {
                ObjectSerializer serializer = serializerSupplier.getSerializer(object);
                collection.put(object, serializer);
                serializer.getConnected().forEach(toCrawl::push);
            }
        }

        return new ArrayList<>(collection.values());
    }

}
