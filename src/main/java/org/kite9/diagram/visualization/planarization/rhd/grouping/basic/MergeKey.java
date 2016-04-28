package org.kite9.diagram.visualization.planarization.rhd.grouping.basic;

import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Group;

/**
 * A merge option is keyed on the two groups it merges.
 */
public class MergeKey {
	
	public MergeKey(Group a, Group b) {
		super();
		this.a = a;
		this.b = b;
	}

	public Group a, b;

	@Override
	public int hashCode() {
		return a.hashCode() + b.hashCode();
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MergeKey other = (MergeKey) obj;
		return ((a == other.b) || (a == other.a)) && ((b == other.b) || (b == other.a));
	}
	
	public String toString() {
		return "[MK: " + a + " (" + a.getSize() + ")  " + b + "(" + b.getSize() + ")]";

	}
}