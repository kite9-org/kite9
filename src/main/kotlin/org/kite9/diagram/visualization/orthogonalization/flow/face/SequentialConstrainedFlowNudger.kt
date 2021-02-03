package org.kite9.diagram.visualization.orthogonalization.flow.face

import org.kite9.diagram.common.algorithms.fg.Node
import org.kite9.diagram.common.algorithms.fg.StateStorage.storeState
import org.kite9.diagram.common.objects.Pair
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.logging.Table
import org.kite9.diagram.visualization.orthogonalization.flow.MappedFlowGraph
import org.kite9.diagram.visualization.planarization.Face
import kotlin.math.abs

/**
 * Handles nudges one-at-a-time, making the assumption that corners are fixed.
 *
 * Because corners are fixed, the remaining freedom to nudge means that you should be
 * able to nudge in either direction.  Given this, we only nudge in the direction that involves
 * least change to the diagram.
 *
 * @author robmoffat
 */
class SequentialConstrainedFlowNudger(facePortionMap: Map<Face, List<PortionNode>>) :
    AbstractConstraintNudger(facePortionMap) {

    override fun processNudges(
        fg: MappedFlowGraph,
        constraintsToNudge: ConstraintGroup,
        faceNodes: MutableCollection<SubdivisionNode>
    ) {
//		 if (true)
//		 return;
        val nudges = Table()
        val logNodes = getTableLogNodes(fg)
        nudges.addRow(logNodes, "comment")
        logSizes(logNodes, nudges, "start", "", "")
        val routesToNudge: MutableCollection<NudgeItem> = createRouteList(constraintsToNudge, fg)
        var failed = 0
        log.send(if (log.go()) null else "Routes to nudge: ", routesToNudge)
        while (routesToNudge.size > 0) {
            if (!performNextNudge(fg, nudges, routesToNudge, faceNodes, logNodes)) {
                failed++

                //throw new LogicException("Could not introduce all nudges");
                return
            }
        }
        log.send(if (log.go()) null else "Failed $failed nudges.  Nudge summary:\n", nudges)
        if (failed > 0) {
            throw LogicException("Could not introduce all nudges")
        }
    }

    /**
     * @return true if the next best nudge could be added
     */
    private fun performNextNudge(
        fg: MappedFlowGraph, nudges: Table, routes: MutableCollection<NudgeItem>, subdivisions: MutableCollection<SubdivisionNode>,
        logNodes: Collection<Node>
    ): Boolean {
        val ni = getNextNudgeItem(routes)!!
        val constraintNumber = ni.id

//		if (constraintNumber==9) {
//			return false;
//		}

        // contains the details of how the faces are divided up
        val splits: MutableList<Pair<SubdivisionNode>> = ArrayList()
        log.send(if (log.go()) null else "Current nudge: $ni")
        subdivideNodes(subdivisions, ni.portionsClockwise, ni.portionsAntiClockwise, splits, constraintNumber, fg)
        log.send(if (log.go()) null else "Current subdivisions: ", displaySubdivisions(subdivisions))
        //addSourceAndSink(ni.portionsClockwise, ni.source, ni.portionsAntiClockwise, ni.sink, fg, subdivisions);
        checkFlowGraphIntegrity(fg, ni.source, ni.sink)
        //log.send(log.go() ? null : "Splits: "+splits);
        //log.send(log.go() ? null : "Current subdivisions: ", displaySubdivisions(subdivisions));
        val state = storeState(fg)
        val ssp = ConstrainedSSP(ni.source, ni.sink, splits)

        // now build the two future possible states
        val cornersBest = calculateCornersRequired(ni, true, true)
        val cornersWorst = calculateCornersRequired(ni, false, true)
        var bestChoice = NudgeChoice(this, fg, state, cornersBest, ni, constraintNumber, subdivisions, ssp)
        var worstChoice = NudgeChoice(this, fg, state, cornersWorst, ni, constraintNumber, subdivisions, ssp)

        // order the choices
        if (abs(cornersBest) == abs(cornersWorst) && worstChoice.evaluate() < bestChoice.evaluate()) {
            val temp = bestChoice
            bestChoice = worstChoice
            worstChoice = temp
        }

        // going to use best route first
        if (bestChoice.evaluate() < Int.MAX_VALUE) {
            bestChoice.apply()
            logSizes(logNodes, nudges, bestChoice.note, "" + bestChoice.cost, "" + worstChoice.cost)
            return true
        }

        // ok, best route didn't work - go for worse route
        if (worstChoice.evaluate() < Int.MAX_VALUE) {
            worstChoice.apply()
            logSizes(logNodes, nudges, worstChoice.note, "" + bestChoice.cost, "" + worstChoice.cost)
            return true
        }
        log.error("Failed Nudge: $ni")

        // neither route worked, undivide and return
//		StateStorage.restoreState(fg, state);
//		routes.add(ni);
//		undivideNodes(subdivisions, splits, constraintNumber);
        //removeSourceAndSink(fg, ni.source, ni.sink);
        return false
    }
}