package org.kite9.diagram.visualization.orthogonalization.flow.balanced

import org.kite9.diagram.common.algorithms.fg.AbsoluteArc
import org.kite9.diagram.common.algorithms.fg.Node

/**
 * Step cost arc is an absolute arc where the cost steps up when absolute flow meets a step point.
 *
 * @author robmoffat
 */
class StepCostArc(
    regularCost: Int,
    capacity: Int,
    from: Node,
    to: Node,
    label: String,
    var stepAt: Int,
    var stepCost: Int
) : AbsoluteArc(regularCost, capacity, from, to, label) {

    override fun getFlowCost(): Int {
        val regularCost = super.getFlowCost()
        val increment = Math.max(0, Math.abs(flow) - stepAt)
        val extraCost = increment * stepCost
        return regularCost + extraCost
    }

    override fun getIncrementalCost(flow: Int): Int {
        val cost = super.getIncrementalCost(flow)
        val origStepCost = Math.max(0, Math.abs(this.flow) - stepAt) * stepCost
        val newStepCost = Math.max(0, Math.abs(this.flow + flow) - stepAt) * stepCost
        val stepCost = newStepCost - origStepCost
        return cost + stepCost
    }
}