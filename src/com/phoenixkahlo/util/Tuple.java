package com.phoenixkahlo.util;

import java.util.Objects;

import com.phoenixkahlo.nodenet.serialization.FieldSerializer;
import com.phoenixkahlo.nodenet.serialization.Serializer;

public class Tuple<A, B> {

	public static Serializer serializer(Serializer subSerializer) {
		return new FieldSerializer(Tuple.class, subSerializer, Tuple::new);
	}
	
	private A a;
	private B b;

	private Tuple() {
	}

	public Tuple(A a, B b) {
		this.a = a;
		this.b = b;
	}

	public A getA() {
		return a;
	}

	public void setA(A a) {
		this.a = a;
	}

	public B getB() {
		return b;
	}

	public void setB(B b) {
		this.b = b;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Tuple)
			return a.equals(((Tuple<?, ?>) other).getA()) && b.equals(((Tuple<?, ?>) other).getB());
		else
			return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(a, b);
	}

}
