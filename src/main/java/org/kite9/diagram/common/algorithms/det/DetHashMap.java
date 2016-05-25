package org.kite9.diagram.common.algorithms.det;

import java.util.HashMap;
import java.util.Map;

public class DetHashMap<K extends Deterministic, V> extends HashMap<K, V> {

	public DetHashMap() {
		super();
	}

	public DetHashMap(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	public DetHashMap(int initialCapacity) {
		super(initialCapacity);
	}

	public DetHashMap(Map<? extends K, ? extends V> m) {
		super(m);
	}
	
}
