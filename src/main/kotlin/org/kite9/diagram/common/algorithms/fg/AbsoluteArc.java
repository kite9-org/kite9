package org.kite9.diagram.common.algorithms.fg;

/**
 * A type of arc where positive or negative flow is permitted, within a boundary, 
 * but any flow in either direction has a constant cost.
 * @author Rob Moffat
 *
 */
public class AbsoluteArc extends AbstractArc {

	protected int cost;
		
	protected int capacity;

	public AbsoluteArc(int cost, int capacity, Node from, Node to, String label) {
		super();
		this.cost = cost;
		this.capacity = capacity;
		this.from = from;
		this.to = to;
		this.label = label;
		from.getArcs().add(this);
		to.getArcs().add(this);
	}

	public int getFlowCost() {
		return Math.abs(flow) * cost;
	}

	public int getIncrementalCost(int flow) {
		int origCost = Math.abs(this.flow)*cost;
		int newCost = Math.abs(this.flow+flow) *cost;
		return newCost - origCost;
	}

	public boolean hasCapacity(boolean reversed) {
		if (isBlocked()) {
			return false;
		}
		if (!reversed) {
			return flow < capacity;
		} else {
			return flow > -capacity;
		}
	}
	
}
