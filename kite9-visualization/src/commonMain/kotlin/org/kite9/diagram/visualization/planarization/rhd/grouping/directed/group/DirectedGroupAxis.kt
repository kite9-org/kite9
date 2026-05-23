/**
 *
 */
package org.kite9.diagram.visualization.planarization.rhd.grouping.directed.group

import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.planarization.rhd.GroupAxis
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.MergePlane

enum class AxisType {
    UNDIRECTED,         // you can choose left, right, up or down for the layout of the CompoundGroup
    NONE,               // no decision - this is a group combining X and Y axes groups or is a leaf
    ALIGNED_IN_AXIS,    // this group dictates the direction within a horizontal or vertical axis, due to a directed edge.
    AGAINST_AXIS        // this group is merging other ALIGNED_IN_AXIS groups together - no axis directed edges.
}

class DirectedGroupAxis(val log: Kite9Log, val g: Group) : GroupAxis {

    var state = MergePlane.UNKNOWN

    override var active = true

    override fun toString(): String {
        return state.toString()
    }

    override var isHorizontal = false
    override var isVertical = false
    var isAxisAligned = false

    var hAxisParentGroup: CompoundGroup? = null
    var vAxisParentGroup: CompoundGroup? = null

    override fun getParentGroup(horizontal: Boolean): CompoundGroup? {
        return if (horizontal) hAxisParentGroup else vAxisParentGroup
    }

    companion object {
        /**
         * Given two groups and the layout between them, this works out what type the merge should be.
         * Effectively, this controls whether two groups are allowed to merge with one another.  If it returns null,
         * the merge isn't allowed.
         */

		fun getMergePlane(a: Group, b: Group): MergePlane? {
            return when (getState(a)) {
                MergePlane.X_FIRST_MERGE -> {
                    return when (getState(b)) {
                        MergePlane.X_FIRST_MERGE, MergePlane.UNKNOWN -> MergePlane.X_FIRST_MERGE
                        MergePlane.Y_FIRST_MERGE -> null
                    }
                }
                MergePlane.Y_FIRST_MERGE -> {
                    return when (getState(b)) {
                        MergePlane.Y_FIRST_MERGE, MergePlane.UNKNOWN -> MergePlane.Y_FIRST_MERGE
                        MergePlane.X_FIRST_MERGE -> null
                    }

                }
                MergePlane.UNKNOWN -> when (getState(b)) {
                    MergePlane.X_FIRST_MERGE -> MergePlane.X_FIRST_MERGE
                    MergePlane.Y_FIRST_MERGE -> MergePlane.Y_FIRST_MERGE
                    MergePlane.UNKNOWN -> {
                        val ld = a.getLink(b)
                        when (ld?.direction) {
                            Direction.UP, Direction.DOWN -> return MergePlane.Y_FIRST_MERGE
                            Direction.LEFT, Direction.RIGHT -> return MergePlane.X_FIRST_MERGE
                            else -> MergePlane.UNKNOWN
                        }
                    }
                }
            }
        }

        /**
         * Only allows the merge if the neighbour is in the right state
         */
		fun compatibleNeighbour(originatingGroup: Group, destinationGroup: Group): Boolean {
            return getMergePlane(originatingGroup, destinationGroup) != null
        }

		fun getState(group: Group): MergePlane {
            return (group.axis as DirectedGroupAxis).state
        }

        fun getType(g: Group): DirectedGroupAxis {
            return g.axis as DirectedGroupAxis
        }
    }

    override fun getAxisType() : AxisType {
        return if (g is CompoundGroup) {
            if ((this.state == MergePlane.X_FIRST_MERGE) || (this.state == MergePlane.Y_FIRST_MERGE)) {
                if (isAxisAligned) {
                    AxisType.ALIGNED_IN_AXIS
                } else {
                    AxisType.AGAINST_AXIS
                }
            } else {
                val ax = g.a.axis as DirectedGroupAxis
                val bx = g.b.axis as DirectedGroupAxis
                if (ax.state != bx.state) {
                    AxisType.NONE
                } else {
                    AxisType.UNDIRECTED
                }
            }
        } else {
            AxisType.NONE
        }
    }
}