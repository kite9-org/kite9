package org.kite9.diagram.common.objects;

public class Pair<X> {

	X a, b;
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Pair<?>) {
			Pair<?> p = (Pair<?>) obj;
			return (safeEquals(p.a, a) && safeEquals(p.b, b)) || 
				(safeEquals(p.b, a) && safeEquals(p.a, b));
		} else {
			return false;
		}
	}

	private boolean safeEquals(Object a2, Object a3) {
		if (a2==a3) {
			return true;
		}
		
		if ((a2==null) || (a3==null)) {
			return false;
		}
		
		return elementEquals(a2, a3);
	}

	protected boolean elementEquals(Object a2, Object a3) {
		return a2.equals(a3);
	}

	@Override
	public int hashCode() {
		return a.hashCode() + b.hashCode();
	}

	@Override
	public String toString() {
		return "["+a+","+b+"]";
	}

	public Pair(X a, X b) {
		this.a = a;
		this.b = b;
	}

	public X getA() {
		return a;
	}

	public X getB() {
		return b;
	}

}
