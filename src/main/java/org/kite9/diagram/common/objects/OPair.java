package org.kite9.diagram.common.objects;

import org.kite9.framework.logging.LogicException;

/**
 * Pair, where values are ordered.  I.e. a tuple of length 2
 * @author robmoffat
 *
 * @param <X>
 */
public class OPair<X> {

	private final X a, b;
	
	public X getA() {
		return a;
	}

	public X getB() {
		return b;
	}

	@Override
	public String toString() {
		return "[" + a + ", " + b + "]";
	}

	public OPair(X from, X b) {
		super();
		this.a = from;
		this.b = b;
	}

	public boolean oneOf(X item) {
		return (a == item) || (b == item);
	}
	
	public X otherOne(X in) {
		if (a==in) {
			return b;
		} else if (b==in) {
			return a;
		} else {
			throw new LogicException();
		}
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((a == null) ? 0 : a.hashCode());
		result = prime * result + ((b == null) ? 0 : b.hashCode());
		return result;
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OPair other = (OPair) obj;
		if (a == null) {
			if (other.a != null)
				return false;
		} else if (!a.equals(other.a))
			return false;
		if (b == null) {
			if (other.b != null)
				return false;
		} else if (!b.equals(other.b))
			return false;
		return true;
	}

}