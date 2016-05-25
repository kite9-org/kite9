package org.kite9.diagram.common.algorithms.fg;

import java.util.Set;

import org.kite9.diagram.common.algorithms.det.Deterministic;

/**
 * A node is a join point for an arc.  
 * @author robmoffat
 *
 */
public interface Node extends Deterministic {
	
	public enum ResidualStatus { SATISFIED, SOURCE, SINK };
	
	public boolean ensureEulersEquilibrium();
	
	public void setType(String type);
	
	public String getType();
	
	public String getId();
	
	public Set<Arc> getArcs();

	public void setArcs(Set<Arc> arcs);
	
	public boolean isLinkedTo(SimpleNode n);
	
	public int getSupply();
	
	public void setSupply(int supply);
	
	public int getFlow();
	
	public void setFlow(int flow);
	
	public void pushFlow(int flow);
	
	public ResidualStatus getResidualStatus();

}
