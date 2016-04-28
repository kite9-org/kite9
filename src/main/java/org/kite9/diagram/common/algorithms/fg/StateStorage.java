package org.kite9.diagram.common.algorithms.fg;

import java.util.HashMap;
import java.util.Map;

/**
 * Allows you to store and restore the state of a flow graph.
 * 
 * @author robmoffat
 *
 */
public class StateStorage {

	public static Map<Object, Integer> storeState(FlowGraph fg) {
		Map<Object, Integer> state = new HashMap<Object, Integer>(100);
		for (Node n : fg.getAllNodes()) {
			state.put(n, n.getFlow());
		}

		for (Arc a : fg.getAllArcs()) {
			state.put(a, a.getFlow());
		}
		
		return state;
	}
	
	public static void restoreState(FlowGraph fg, Map<Object, Integer> state) {
		for (Node n : fg.getAllNodes()) {
			int flow = state.get(n);
			n.setFlow(flow);
			n.setSupply(-flow);
		}

		for (Arc a : fg.getAllArcs()) {
			Integer val = state.get(a);
			if (val!=null)
				a.setFlow(val);
		}
	}
}
