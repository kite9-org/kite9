package org.kite9.diagram.common.algorithms.fg;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Basic data object class for containing a flow network
 * 
 * @author robmoffat
 *
 */
public class FlowGraph {

	protected List<Node> allNodes;
	
	protected Set<Arc> allArcs = new HashSet<Arc>();

	public FlowGraph(List<Node> allNodes) {
		super();
		this.allNodes = allNodes;
	}

	@Override
	public String toString() {
		return "[FlowGraph:\n"+
		"\tnodes:"+allNodes+
		"\n\tarcs:"+getAllArcs()+"]";
	}
	
	public Set<Arc> getAllArcs() {
		return allArcs;
 	}
	
	public List<Node> getAllNodes() {
		return allNodes;
	}

	
}
