package org.kite9.diagram.visualization.planarization.rhd.grouping.directed.group

import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.AbstractCompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.MergePlane
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.merge.DirectedMergeState

class DirectedCompoundGroup(
    a: Group,
    b: Group,
    treatAsLeaf: Boolean,
    groupNumber: Int,
    size: Int,
    hc: Int,
    bs: DirectedMergeState,
    log: Kite9Log,
    alignedDirection: Direction?
) : AbstractCompoundGroup(
    a,b, treatAsLeaf, groupNumber, size, hc) {

    override val axis : DirectedGroupAxis = buildCompoundAxis(a, b, alignedDirection, log);

    override val linkManager : DirectedLinkManager  = DirectedLinkManager(bs, this)

    override val internalLinkA = fileLinks(a, b)

    override val internalLinkB = fileLinks(b, a)

    private fun buildCompoundAxis(
        a: Group,
        b: Group,
        alignedDirection: Direction?,
        log: Kite9Log
    ) : DirectedGroupAxis {
        val used = DirectedGroupAxis(log,  this)
        var axis = DirectedGroupAxis.getMergePlane(a, b)
        if (axis === MergePlane.UNKNOWN) {
            if (alignedDirection != null) {
                axis = when (alignedDirection) {
                    Direction.UP, Direction.DOWN -> MergePlane.Y_FIRST_MERGE
                    Direction.LEFT, Direction.RIGHT -> MergePlane.X_FIRST_MERGE
                }
            }
        }
        if (axis == null) {
            /* this is where we combine two completed compound groups
             of different axes
             */
            axis = MergePlane.UNKNOWN
        }
        when (axis) {
            MergePlane.X_FIRST_MERGE -> {
                used.state = MergePlane.X_FIRST_MERGE
                used.isHorizontal = false
                used.isVertical = true
            }
            MergePlane.Y_FIRST_MERGE -> {
                used.state = MergePlane.Y_FIRST_MERGE
                used.isHorizontal = true
                used.isVertical = false
            }
            null,
            MergePlane.UNKNOWN -> {
                used.state = MergePlane.UNKNOWN
                used.isVertical = true
                used.isHorizontal = true
            }
        }

        return used
    }


}