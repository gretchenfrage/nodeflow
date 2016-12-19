package com.phoenixkahlo.util;

import java.util.Objects;

import com.phoenixkahlo.nodenet.serialization.FieldSerializer;
import com.phoenixkahlo.nodenet.serialization.Serializer;

/**
 * An immutable set of two unordered items.
 */
public class UnorderedTuple<E extends PerfectHashable> {

	public static Serializer serializer(Serializer subSerializer) {
		return new FieldSerializer(UnorderedTuple.class, subSerializer, UnorderedTuple::new);
	}

	// item1 must always store the item with the lesser hash.
	private E item1;
	private E item2;

	private UnorderedTuple() {
	}

	public UnorderedTuple(E item1, E item2) {
		if (item1.hashCode() < item2.hashCode()) {
			this.item1 = item1;
			this.item2 = item2;
		} else {
			this.item1 = item2;
			this.item2 = item1;
		}
	}

	public boolean contains(E item) {
		return item1.equals(item) || item2.equals(item);
	}

	public E get1() {
		return item1;
	}

	public E get2() {
		return item2;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof UnorderedTuple)
			return item1.equals(((UnorderedTuple<?>) other).item1) && item2.equals(((UnorderedTuple<?>) other).item2);
		else
			return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(item1, item2);
	}

}
