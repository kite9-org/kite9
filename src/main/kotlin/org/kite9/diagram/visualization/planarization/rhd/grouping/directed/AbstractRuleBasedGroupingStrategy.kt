package org.kite9.diagram.visualization.planarization.rhd.grouping.directed

import org.kite9.diagram.common.elements.grid.GridPositioner
import org.kite9.diagram.common.elements.mapping.ElementMapper
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.AbstractGroupingStrategy
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.merge.BasicMergeState
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.group.DirectedGroupAxis
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.group.DirectedLinkManager
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.merge.DirectedMergeState
import org.kite9.diagram.visualization.planarization.rhd.links.ContradictionHandler

/**
 * This class encapsulates the rules about directional merging.
 *
 * When groups have directional edges, they will need to be merged in both axes.  The DirectedGroupTypeState
 * stores the state of this.
 *
 * If directional merging is going on (X_FIRST, Y_FIRST), groups need to exhaust the single directed merges in a given
 * direction first, before beginning the single directed merges in the other direction.
 *
 * If all single directed merges are exhausted, groups may buddy merge.  That is, where two groups
 * have a common neighbour in the same direction, they can merge together, which means that the common neighbour
 * now has a single group to merge with.
 *
 * Where groups don't have directed edges leaving them (UNDIRECTED), they can merge happily with any group not doing X_FIRST or Y_FIRST
 * merging.
 *
 * When all the directed edges are consumed and merged into single X_FIRST and Y_FIRST groups, those
 * are themselves merged together into a compound group (UNDIRECTED).
 *
 * About Axis / Perpendicular:
 *
 * Let's say we are doing Y-first merge.  The axis is vertical.  The axis part of the merge is about creating columns through the diagram
 * where things are aligned.   The perpendicular part is then about grouping those columns together.
 * The requirements are different in each case.  Axis merging is all about creating columns, so it's more interested in single directed merges.
 * However, perpendicular is all about keeping containers together, so priorities are based on whether you are completing a container or not.
 *
 *
 * @author robmoffat
 */
abstract class AbstractRuleBasedGroupingStrategy(
    top: DiagramElement,
    elements: Int,
    ch: ContradictionHandler,
    gp: GridPositioner,
    em: ElementMapper
) : AbstractGroupingStrategy(top, elements, ch, gp, em) {

    override fun canGroupsMerge(
        a: Group,
        b: Group,
        ms: BasicMergeState,
        alignedGroup: Group?,
        alignedSide: Direction?
    ): Int {
        //log.send("Testing merge: \n\t"+a+"\n\t"+b+"\n\t"+alignedGroup+"\n\t"+alignedSide);
        var alignedGroup = alignedGroup
        alignedGroup = alignedGroup?.let { getWorkingGroup(it) }
        val out = super.canGroupsMerge(a, b, ms, alignedGroup, alignedSide)
        if (out == INVALID_MERGE) {
            return out
        }
        val plane = DirectedGroupAxis.getMergePlane(a, b)
        if (plane == null || alreadyMerged(a, plane) || alreadyMerged(b, plane)) {
            // means that the groups are now incompatible.
            return INVALID_MERGE
        }

        // if there is a contradiction, record it and quit
        val hasContradiction = checkContradiction(a, b, plane)
        if (hasContradiction) {
            return ILLEGAL_PRIORITY
        }

        // figure out if the axis is horizontal or vertical first.
        var horizontalMergesFirst = true
        if (DirectedGroupAxis.getType(a).state === MergePlane.Y_FIRST_MERGE
            || DirectedGroupAxis.getType(b).state === MergePlane.Y_FIRST_MERGE
            || plane === MergePlane.Y_FIRST_MERGE
        ) {
            horizontalMergesFirst = false
        }
        for (pr in getRules(ms as DirectedMergeState)) {
            val p = pr.getMergePriority(a, b, ms, alignedGroup, alignedSide, plane, horizontalMergesFirst)
            if (p != PriorityRule.UNDECIDED) {
                return p
            }
        }
        return ILLEGAL_PRIORITY
    }

    private fun alreadyMerged(b: Group, plane: MergePlane): Boolean {
        return if (plane === MergePlane.X_FIRST_MERGE) {
            b.axis.getParentGroup(false) != null
        } else if (plane === MergePlane.Y_FIRST_MERGE) {
            b.axis.getParentGroup(true) != null
        } else {
            false
        }
    }

    protected abstract fun getRules(ms: DirectedMergeState): List<PriorityRule>

    fun getWorkingGroup(group: Group?): Group? {
        var group: Group? = group ?: return null
        var axis: DirectedGroupAxis?
        var parent: Group? = null
        do {
            if (parent != null) {
                group = parent
            }
            axis = group!!.axis as DirectedGroupAxis
            parent = when (axis.state) {
                MergePlane.X_FIRST_MERGE -> axis.vertParentGroup
                MergePlane.Y_FIRST_MERGE -> axis.horizParentGroup
                else -> {
                    if (axis.horizParentGroup != axis.vertParentGroup) {
                        throw LogicException("Not decided")
                    }
                    axis.horizParentGroup
                }
            }
        } while (parent != null)
        return group
    }

    private fun checkContradiction(
        a: Group,
        b: Group,
        axis: MergePlane
    ): Boolean {
        val directedEdgesOnly = DirectedLinkManager.createMask(
            axis,
            false,
            false,
            Direction.UP,
            Direction.DOWN,
            Direction.LEFT,
            Direction.RIGHT
        )
        val blm = b.linkManager
        val subset = a.linkManager.subset(directedEdgesOnly)
        for (gald in subset) {
            val gbld = blm[gald.group]
            if (gbld != null && gbld.direction != null && gbld.direction !== gald.direction) {
                return true
            }
        }
        return false
    }

    companion object {
        // different types of merges get different priorities.
		@JvmField
		var AXIS_SINGLE_NEIGHBOUR = 0
        @JvmField
		var AXIS_ALIGNED = 1
        @JvmField
        var AXIS_MULTI_NEIGHBOUR = 2
        @JvmField
		var PERP_NEIGHBOUR = 3
        @JvmField
		var PERP_ALIGNED = 4
        @JvmField
        var AXIS_ALIGNED_UNSURE = 5
        @JvmField
		var UNDIRECTED_LINKED = 6
        @JvmField
		var UNDIRECTED_ALIGNED = 7
        @JvmField
		var UNCONNECTED_NEIGHBOUR = 8
    }
}