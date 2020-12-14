package org.kite9.diagram.common.algorithms.fg;


/**
 * A linear arc has a constant cost for each item of flow pushed into it.
 * It has upper and lower bounds on the amount of flow that can travel through it.
 * @author Rob Moffat
 *
 */
public class LinearArc extends AbstractArc {

	protected int cost;
	
	protected int lowerBound;
	
	protected int capacity;

	public LinearArc(int cost, int capacity, int lowerBound, Node from, Node to, String label) {
		super();
		this.cost = cost;
		this.capacity = capacity;
		this.from = from;
		this.to = to;
		this.lowerBound = lowerBound;
		this.label = label;
		from.getArcs().add(this);
		to.getArcs().add(this);
	}

	public int getIncrementalCost(int flow) {
		return flow * cost;
	}

	public boolean hasCapacity(boolean reversed) {
		if (!reversed) {
			return flow < capacity;
		} else {
			return flow > lowerBound;
		}
	}
	
}
