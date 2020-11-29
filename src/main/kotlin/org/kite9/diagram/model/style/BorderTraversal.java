package org.kite9.diagram.model.style;

public enum BorderTraversal {

	ALWAYS, LEAVING, NONE;
	
	public static BorderTraversal reduce(BorderTraversal a, BorderTraversal b) {
		if (a==null) {
			return b;
		} else if (b == null) {
			return a;
		} else if ((a != ALWAYS) && (b != ALWAYS)) {
			return NONE;
		} else {	
			return ALWAYS;
		}
	}
	
}
