package org.kite9.diagram.visualization.orthogonalization.flow.balanced;

import org.kite9.diagram.common.algorithms.fg.AbsoluteArc;
import org.kite9.diagram.common.algorithms.fg.Node;

/**
 * Step cost arc is an absolute arc where the cost steps up when absolute flow meets a step point. 
 * 
 * @author robmoffat
 *
 */
public class StepCostArc extends AbsoluteArc {

	public int getStepAt() {
		return stepAt;
	}

	public void setStepAt(int stepAt) {
		this.stepAt = stepAt;
	}

	public int getStepCost() {
		return stepCost;
	}

	public void setStepCost(int stepCost) {
		this.stepCost = stepCost;
	}

	private int stepAt;
	private int stepCost;
	
	public StepCostArc(int regularCost, int capacity, Node from, Node to, String label, int stepAt, int stepCost) {
		super(regularCost, capacity, from, to, label);
		this.stepAt = stepAt;
		this.stepCost = stepCost;
	}

	@Override
	public int getFlowCost() {
		int regularCost = super.getFlowCost();
		
		int increment = Math.max(0, Math.abs(getFlow()) - stepAt);
		int extraCost = increment * stepCost;
		
		return regularCost+extraCost;
	}

	@Override
	public int getIncrementalCost(int flow) {
		int cost = super.getIncrementalCost(flow);
		
		int origStepCost = Math.max(0, Math.abs(this.getFlow())-stepAt)*stepCost;
		int newStepCost = Math.max(0, Math.abs(this.getFlow() + flow)-stepAt)*stepCost;
		int stepCost = newStepCost - origStepCost;
		return cost + stepCost;
	}

	
}
