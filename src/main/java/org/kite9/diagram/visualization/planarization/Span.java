package org.kite9.diagram.visualization.planarization;

import org.kite9.framework.common.Kite9ProcessingException;

/**
 * Hierarchical fraction span.  
 * 
 * Allows you to subdivide a span into sections of integer length, and have 
 * further subdivisions of those lengths, and so on.  
 * 
 * This avoids the issue of subdividing a double, say, and running out of precision 
 * eventually.
 * 
 * @author robmoffat
 *
 */
public class Span implements Comparable<Span> {
	
	private Span parent;
	private int num;
	private int denom;
	
	public Span(int num, int denom) {
		this.num = num;
		this.denom = denom;
	}
	
	public Span(int num, int denom, Span parent) {
		this (num, denom);
		this.parent = parent;
	}

	@Override
	public int compareTo(Span o) {
		return compare(this, o);
	}
	
	public int depth() {
		if (parent == null) {
			return 1;
		} else {
			return parent.depth() + 1;
		}
	}
	
	private static int compare(Span a, Span b) {
		if (a.depth() > b.depth()) {
			int c = compare(a.parent, b);
			if (c != 0) {
				return c;
			} else {
				return 1;
			}
		} else if (a.depth() < b.depth()) {
			int c = compare(a, b.parent);
			if (c != 0) {
				return c;
			} else {
				return -1;
			}
		} else if ((a.parent != null) && (b.parent != null)) {
			int c = compare(a.parent, b.parent);
			if (c != 0) {
				return c;
			}
		}
		
		if (a.denom != b.denom) {
			throw new Kite9ProcessingException("Comparing different spans");
		}
 		
		return ((Integer)a.num).compareTo(b.num);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + denom;
		result = prime * result + num;
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
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
		Span other = (Span) obj;
		if (denom != other.denom)
			return false;
		if (num != other.num)
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		return true;
	}

	public static float convert(float start, float end, int num, int denom, Span parent) {
		if (parent == null) {
			float range = end-start;
			float frac = (range / (float) denom) * (float) num;
			return start + frac;
		} else {
			float newStart = convert(start, end, parent.num, parent.denom, parent.parent);
			float newEnd = convert(start, end, parent.num+1, parent.denom, parent.parent);
			return convert(newStart, newEnd, num, denom, null);
		}
	}
	
	/**
	 * Converts the position of this fraction into a point along a line.
	 */
	public float convert(float range) {
		return convert(0f, range, num, denom, parent);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		compileString(this, sb);
		return sb.toString();
	}
	
	private static void compileString(Span s, StringBuilder sb) {
		if (s.parent != null) {
			compileString(s.parent, sb);
		}
		if (sb.length() != 0) {
			sb.append(".");
		}
		sb.append(s.num);
		sb.append("/");
		sb.append(s.denom);
	}
}
