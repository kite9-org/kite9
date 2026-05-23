package org.kite9.diagram.visualization.planarization.rhd.grouping.directed.group

import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.AbstractCompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.LayoutSetPoint
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.MergePlane
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.merge.DirectedMergeState

class DirectedCompoundGroup(
        a: Group,
        b: Group,
        completeMerge: Boolean,
        groupNumber: Int,
        size: Int,
        hc: Int,
        bs: DirectedMergeState,
        log: Kite9Log,
        alignedDirection: Direction?,
        singleDirectedAxisMerge: Boolean
) : AbstractCompoundGroup(a, b, completeMerge, groupNumber, size, hc) {

    override val axis: DirectedGroupAxis = buildCompoundAxis(a, b, alignedDirection, log, singleDirectedAxisMerge)

    override val linkManager: DirectedLinkManager = DirectedLinkManager(bs, this)

    override val internalLinkA = fileLinks(a, b)

    override val internalLinkB = fileLinks(b, a)

    private fun buildCompoundAxis(
            a: Group,
            b: Group,
            alignedDirection: Direction?,
            log: Kite9Log,
            singleDirectedAxisMerge: Boolean
    ): DirectedGroupAxis {
        val used = DirectedGroupAxis(log, this)
        var axis = DirectedGroupAxis.getMergePlane(a, b)
        if (axis === MergePlane.UNKNOWN) {
            if (alignedDirection != null) {
                axis =
                        when (alignedDirection) {
                            Direction.UP, Direction.DOWN -> MergePlane.Y_FIRST_MERGE
                            Direction.LEFT, Direction.RIGHT -> MergePlane.X_FIRST_MERGE
                        }
            }
        }
        /* this is where we combine two completed compound groups
        of different axes
        */
        when (axis) {
            MergePlane.X_FIRST_MERGE -> {
                used.state = MergePlane.X_FIRST_MERGE
                used.isHorizontal = true
                used.isVertical = false
                used.isAxisAligned = singleDirectedAxisMerge
            }
            MergePlane.Y_FIRST_MERGE -> {
                used.state = MergePlane.Y_FIRST_MERGE
                used.isHorizontal = false
                used.isVertical = true
                used.isAxisAligned = singleDirectedAxisMerge
            }
            null, MergePlane.UNKNOWN -> {
                used.state = MergePlane.UNKNOWN
                used.isVertical = true
                used.isHorizontal = true
            }
        }

        return used
    }

    /**
     * Directed compound groups have an axis, so layouts
     * must respect the axis.
     */
    override fun setLayout(l: Layout?, t: LayoutSetPoint) {
        if (Layout.isHorizontal(l)) {
            if (this.axis.isHorizontal) {
                super.setLayout(l, t)
            }
        } else {
            if (this.axis.isVertical) {
                super.setLayout(l, t)
            }
        }
    }
}
