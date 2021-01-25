package org.kite9.diagram.visualization.orthogonalization.flow.face

import org.kite9.diagram.common.algorithms.fg.StateStorage.restoreState
import org.kite9.diagram.common.algorithms.fg.StateStorage.storeState
import org.kite9.diagram.visualization.orthogonalization.flow.MappedFlowGraph

internal class NudgeChoice(
    private val abstractConstraintNudger: AbstractConstraintNudger,
    val fg: MappedFlowGraph,
    val stateBefore: Map<Any, Int>,
    val corners: Int,
    val ni: NudgeItem,
    val constraintNumber: Int,
    val subdivisions: Collection<SubdivisionNode>,
    val ssp: ConstrainedSSP
) {

    var stateAfter: Map<Any, Int>? = null

    @JvmField
    var cost: Int? = null

    var note: String
    fun evaluate(): Int {
        if (cost == null) {
            if (corners == 0) {
                stateAfter = stateBefore
                cost = 0
            } else {
                restoreState(fg, stateBefore)
                cost = abstractConstraintNudger.introduceConstraints(
                    fg, ni, constraintNumber, corners, note, ni.source, ni.sink, subdivisions,
                    ssp
                )
                stateAfter = storeState(fg)
            }
        }
        return cost!!
    }

    fun apply() {
        if (stateAfter == null) {
            evaluate()
        }
        restoreState(fg, stateAfter!!)
    }

    init {
        note = constraintNumber.toString() + " nudge = " + ni.id + " corners: " + corners
    }
}