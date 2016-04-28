package org.kite9.diagram.common.objects;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Like a pair, but contains four items.
 * @author robmoffat
 *
 * @param <X>
 */
public class Rectangle<X> {

	@Override
	public String toString() {
		return a+" "+b+" "+c+" "+d;
	}

	X a,b,c,d;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((a == null) ? 0 : a.hashCode());
		result = prime * result + ((b == null) ? 0 : b.hashCode());
		result = prime * result + ((c == null) ? 0 : c.hashCode());
		result = prime * result + ((d == null) ? 0 : d.hashCode());
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
		Rectangle<?> other = (Rectangle<?>) obj;
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
		if (c == null) {
			if (other.c != null)
				return false;
		} else if (!c.equals(other.c))
			return false;
		if (d == null) {
			if (other.d != null)
				return false;
		} else if (!d.equals(other.d))
			return false;
		return true;
	}

	public X getA() {
		return a;
	}

	public X getB() {
		return b;
	}

	public X getC() {
		return c;
	}

	public X getD() {
		return d;
	}

	public Rectangle(X a, X b, X c, X d) {
		super();
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
	}
	
	public Collection<X> getAll() {
		ArrayList<X> out =  new ArrayList<X>(4);
		out.add(a);
		out.add(b);
		out.add(c);
		out.add(d);
		return out;
	}
}
