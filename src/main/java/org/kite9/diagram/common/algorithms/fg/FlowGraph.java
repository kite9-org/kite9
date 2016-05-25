package org.kite9.diagram.common.algorithms.fg;

import java.util.List;
import java.util.Set;

import org.kite9.diagram.common.algorithms.det.DetHashSet;
import org.kite9.diagram.common.algorithms.det.UnorderedSet;

/**
 * Basic data object class for containing a flow network
 * 
 * @author robmoffat
 *
 */
public class FlowGraph {

	protected List<Node> allNodes;
	
	protected Set<Arc> allArcs = new DetHashSet<Arc>();

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
