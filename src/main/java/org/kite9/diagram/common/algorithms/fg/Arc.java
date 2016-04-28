package org.kite9.diagram.common.algorithms.fg;

public interface Arc {

	public abstract int getFlowFrom(Node n);

	public abstract void pushFlow(int flow);

	public abstract Node getFrom();

	public abstract Node getTo();
	
	public void setFrom(Node from);

	public void setTo(Node to);
	
	public int getIncrementalCost(int flow);
	
	public int getFlow();
	
	public void setFlow(int flow);
	
	public Node otherEnd(Node n);
	
	public void setBlocked(boolean blocked);
	
	public boolean isBlocked();
	
	public boolean hasCapacity(boolean reversed);

}