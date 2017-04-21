package com.phoenixkahlo.nodenet.smartserial;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Top-level serializer object.
 */
public class Serializer {

    private ObjectSerializerSupplier serializerSupplier;

    public Serializer(ObjectSerializerSupplier serializerSupplier) {
        this.serializerSupplier = serializerSupplier;
    }

    public byte[] serialize(Object object) {
        // Step 1
        List<ObjectSerializer> serializers = ObjectGraphCrawler.crawl(object, serializerSupplier);

        // Organization
        class SerialUnit {
            ObjectSerializer serializer;
            List<ByteSequenceFormer> sequenceFormers;
            byte[] byteSequence;

            SerialUnit(ObjectSerializer serializer) {
                this.serializer = serializer;
            }
        }
        List<SerialUnit> units = serializers.stream().map(SerialUnit::new).collect(Collectors.toList());

        // Step 2
        for (SerialUnit unit : units) {
            unit.sequenceFormers = unit.serializer.getSequence();
        }

        // Step 3
        Map<ByteSequenceFormer, List<SerialUnit>> clusters = new HashMap<>();
        for (SerialUnit unit : units) {
            ByteSequenceFormer id = unit.serializer.getID();
            if (!clusters.containsKey(id))
                clusters.put(id, new ArrayList<>());
            clusters.get(id).add(unit);
        }


    }

}
