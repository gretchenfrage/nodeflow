package com.phoenixkahlo.util;

import java.util.Objects;

public class Tuple<A, B> {

	private A a;
	private B b;
	
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
