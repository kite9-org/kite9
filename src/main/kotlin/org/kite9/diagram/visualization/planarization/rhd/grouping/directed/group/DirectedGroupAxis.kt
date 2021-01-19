/**
 *
 */
package org.kite9.diagram.visualization.planarization.rhd.grouping.directed.group

import org.kite9.diagram.common.elements.RoutingInfo
import org.kite9.diagram.common.objects.Bounds
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.model.position.Layout.Companion.reverse
import org.kite9.diagram.visualization.planarization.rhd.GroupAxis
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.LeafGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.MergePlane
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D

class DirectedGroupAxis(val log: Kite9Log, val g: Group) : GroupAxis {

    override var isLayoutRequired = true

    var state = MergePlane.UNKNOWN

    override var active = true

    override fun toString(): String {
        return state.toString()
    }

    override var isHorizontal = false
    override var isVertical = false

    var horizParentGroup: CompoundGroup? = null
    var vertParentGroup: CompoundGroup? = null

    override fun getParentGroup(horizontal: Boolean): CompoundGroup? {
        return if (horizontal) horizParentGroup else vertParentGroup
    }

    fun isSet(rh: RoutableHandler2D?, ri: Bounds?, horiz: Boolean): Boolean {
        return ri != null
    }

    fun getPosition1D(rh: RoutableHandler2D, temp: Boolean, horiz: Boolean): Bounds {
        var ri = rh.getPosition(g!!, horiz)

        if ((ri == null) && temp) {
            ri = rh.getTempPosition(g!!, horiz)
        }

        return if (ri == null) {
            // ok, calculate via parent groups.  
            var out: Bounds? = null
            val parent: Group? =
                if (horiz) horizParentGroup else vertParentGroup
            out = if (parent != null) {
                if (parent.axis.isLayoutRequired) {
                    val l = getLayoutFor(parent, g)
                    rh.narrow(l, (parent.axis as DirectedGroupAxis).getPosition1D(rh, temp, horiz)!!, horiz, true)
                } else {
                    (parent.axis as DirectedGroupAxis).getPosition1D(rh, temp, horiz)
                }
            } else {
                // no parent group = top
                rh.getTopLevelBounds(horiz)
            }

            //log.send("Setting "+(temp? "temp" : "real") + (horiz ? "horiz" : "vert")+" position for "+g+"\n\t"+out);
            if (temp) {
                rh.setTempPosition(g, out!!, horiz)
            } else {
                log.send("Placed: " + g!!.groupNumber + " " + horiz + " " + out)
                rh.setPlacedPosition(g, out!!, horiz)
            }
            if (!temp && g is LeafGroup) {
                // this means the routable handler also has final positions for each contained element
                rh.setPlacedPosition((g as LeafGroup).contained, out, horiz)
            }
            out
        } else {
            ri
        }
    }

    override fun getPosition(rh: RoutableHandler2D, temp: Boolean): RoutingInfo {
        val xBounds = getPosition1D(rh, temp, true)
        val yBounds = getPosition1D(rh, temp, false)
        return rh.createRouting(xBounds, yBounds)
    }

    override fun isReadyToPosition(completedGroups: Set<Group>): Boolean {
        val hready = horizParentGroup == null || completedGroups.contains(horizParentGroup)
        val vready = vertParentGroup == null || completedGroups.contains(vertParentGroup)
        return hready && vready
    }

    companion object {
        /**
         * Given two groups and the layout between them, this works out what type the merge should be.
         * Effectively, this controls whether two groups are allowed to merge with one another.  If it returns null,
         * the merge isn't allowed.
         */
		@JvmStatic
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
                        if (ld != null && ld.direction != null) {
                            when (ld.direction) {
                                Direction.UP, Direction.DOWN -> return MergePlane.Y_FIRST_MERGE
                                Direction.LEFT, Direction.RIGHT -> return MergePlane.X_FIRST_MERGE
                            }
                        }
                        MergePlane.UNKNOWN
                    }
                }
            }
        }

        /**
         * Only allows the merge if the neighbour is in the right state
         */
		@JvmStatic
		fun compatibleNeighbour(originatingGroup: Group, destinationGroup: Group): Boolean {
            return getMergePlane(originatingGroup, destinationGroup) != null
        }

        fun inState(group: Group, vararg okStates: Any): Boolean {
            for (i in 0 until okStates.size) {
                if (getState(group) === okStates[i]) {
                    return true
                }
            }
            return false
        }

        @JvmStatic
		fun getState(group: Group): MergePlane {
            return (group.axis as DirectedGroupAxis).state
        }

        fun getLayoutFor(`in`: Group, g: Group?): Layout? {
            val ax = getType(`in`)
            return if (`in`.layout == null) {
                null
            } else when (`in`.layout) {
                Layout.LEFT, Layout.RIGHT -> if (ax.isHorizontal) layoutSide(
                    `in`,
                    g
                ) else null
                Layout.UP, Layout.DOWN -> if (ax.isVertical) layoutSide(
                    `in`,
                    g
                ) else null
                else ->            //HORIZONTAL, VERTICAL:
                    `in`.layout
            }
        }

        private fun layoutSide(`in`: Group, g: Group?): Layout? {
            val cg = `in` as CompoundGroup
            return if (cg.b === g) {
                cg.layout
            } else if (cg.a === g) {
                reverse(cg.layout)
            } else {
                throw LogicException()
            }
        }

        @JvmStatic
		fun getType(g: Group): DirectedGroupAxis {
            return g.axis as DirectedGroupAxis
        }
    }
}