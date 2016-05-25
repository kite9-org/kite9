package org.kite9.diagram.common.algorithms.fg;

import java.util.Set;

import org.kite9.diagram.common.algorithms.det.DetHashSet;

/**
 * Basic implementation of the flow graph node
 * @author robmoffat
 *
 */
public class SimpleNode implements Node {
	
	private String id;
	
	private int supply;
	
	private String type;
	
	private Object representation;
	
	public Object getRepresentation() {
		return representation;
	}

	int flow = 0;
	
	public SimpleNode(String id, int supply, Object memento) {
		this.id = id;
		this.supply = supply;
		this.representation = memento;
	}
	
	private Set<Arc> arcs = new DetHashSet<Arc>();

	public Set<Arc> getArcs() {
		return arcs;
	}

	public void setArcs(Set<Arc> arcs) {
		this.arcs = arcs;
	}

	@Override
	public String toString() {
		return id+"("+flow+"/"+supply+")";
	}

	public String getId() {
		return id;
	}
	
	public boolean isLinkedTo(SimpleNode n) {
		for (Arc a : arcs) {
			if (a.otherEnd(this)==n)
				return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}

	public int getSupply() {
		return supply;
	}
	
	public boolean ensureEulersEquilibrium() {
		return flow + supply == 0;

	}
	
	public int getFlow() { 
		return flow;
	}

	public void setSupply(int supply) {
		this.supply = supply;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void pushFlow(int flow) {
		this.flow += flow;
	}
	
	public void setFlow(int flow) {
		this.flow = flow;
	}

	public ResidualStatus getResidualStatus() {
		int amt = getSupply()+getFlow();
		if (amt<0) {
			return ResidualStatus.SINK;
		} else if (amt>0) {
			return ResidualStatus.SOURCE;
		} else {
			return ResidualStatus.SATISFIED;
		}
	}
}
