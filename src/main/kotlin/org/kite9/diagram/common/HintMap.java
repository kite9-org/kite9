package org.kite9.diagram.common;

import java.util.HashMap;
import java.util.Map;

public class HintMap extends HashMap<String, Float>{

	private static final long serialVersionUID = 8622279690962224111L;

	public HintMap() {
		super();
	}

	public HintMap(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	public HintMap(int initialCapacity) {
		super(initialCapacity);
	}

	public HintMap(Map<? extends String, ? extends Float> m) {
		super(m);
	}

}
