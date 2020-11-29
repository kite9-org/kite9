package org.kite9.diagram.common.algorithms.det;

import java.util.Collection;
import java.util.HashSet;

public class DetHashSet<K extends Deterministic> extends HashSet<K>{

	public DetHashSet() {
		super();
	}

	public DetHashSet(Collection<? extends K> c) {
		super(c);
	}

	public DetHashSet(int initialCapacity) {
		super(initialCapacity);
	}

	
}
