package org.kite9.diagram.visualization.orthogonalization.flow.balanced;

import org.kite9.diagram.common.algorithms.fg.AbsoluteArc;
import org.kite9.diagram.common.algorithms.fg.Node;

/**
 * Allows you to set the flow at which cost becomes zero. 
 * 
 * @author robmoffat
 *
 */
public class NonZeroArc extends AbsoluteArc {

	private int zeroAt;
	
	public NonZeroArc(int regularCost, int capacity, Node from, Node to, String label, int zeroAt) {
		super(regularCost, capacity, from, to, label);
		this.zeroAt = zeroAt;
	}

	@Override
	public int getFlowCost() {
		return Math.abs(getFlow()-zeroAt) * getCost();
	}

	@Override
	public int getIncrementalCost(int flow) {
		int origCost = Math.abs(this.getFlow()-zeroAt)*getCost();
		int newCost = Math.abs(this.getFlow()+flow-zeroAt) * getCost();
		return newCost - origCost;
	}

	
}
