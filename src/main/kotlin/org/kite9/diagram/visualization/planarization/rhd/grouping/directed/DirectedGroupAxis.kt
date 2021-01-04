/**
 *
 */
package org.kite9.diagram.visualization.planarization.rhd.grouping.directed

import org.kite9.diagram.common.elements.RoutingInfo
import org.kite9.diagram.common.objects.Bounds
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.visualization.planarization.rhd.GroupAxis
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.LeafGroup
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D

class DirectedGroupAxis(private val log: Kite9Log) : GroupAxis {

    var g: GroupPhase.Group? = null

    override fun setGroup(g: GroupPhase.Group) {
        this.g = g
    }

    override var isLayoutRequired = true

    @JvmField
	var state = MergePlane.UNKNOWN

    override var isActive = true
    override fun toString(): String {
        return state.toString()
    }

    override var isHorizontal = false
    override var isVertical = false
    @JvmField
	var horizParentGroup: CompoundGroup? = null
    @JvmField
	var vertParentGroup: CompoundGroup? = null
    override fun getParentGroup(horizontal: Boolean): CompoundGroup? {
        return if (horizontal) horizParentGroup else vertParentGroup
    }

    fun isSet(rh: RoutableHandler2D?, ri: Bounds?, horiz: Boolean): Boolean {
        return if (ri == null) {
            false
        } else {
            true
        }
    }

    fun getPosition1D(rh: RoutableHandler2D, temp: Boolean, horiz: Boolean): Bounds? {
        var ri = rh.getPosition(g, horiz)
        if (!isSet(rh, ri, horiz) && temp) {
            ri = rh.getTempPosition(g, horiz)
        }
        return if (!isSet(rh, ri, horiz)) {
            // ok, calculate via parent groups.  
            var out: Bounds? = null
            val parent: GroupPhase.Group? =
                if (horiz) horizParentGroup else vertParentGroup
            out = if (parent != null) {
                if (parent.axis.isLayoutRequired) {
                    val l = getLayoutFor(parent, g)
                    rh.narrow(l, (parent.axis as DirectedGroupAxis).getPosition1D(rh, temp, horiz), horiz, true)
                } else {
                    (parent.axis as DirectedGroupAxis).getPosition1D(rh, temp, horiz)
                }
            } else {
                // no parent group = top
                rh.getTopLevelBounds(horiz)
            }

            //log.send("Setting "+(temp? "temp" : "real") + (horiz ? "horiz" : "vert")+" position for "+g+"\n\t"+out);
            if (temp) {
                rh.setTempPosition(g, out, horiz)
            } else {
                log.send("Placed: " + g!!.groupNumber + " " + horiz + " " + out)
                rh.setPlacedPosition(g, out, horiz)
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

    override fun isReadyToPosition(completedGroups: Set<GroupPhase.Group>): Boolean {
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
		fun getMergePlane(a: GroupPhase.Group, b: GroupPhase.Group): MergePlane? {
            return when (getState(a)) {
                MergePlane.X_FIRST_MERGE -> {
                    return when (getState(b)) {
                        MergePlane.X_FIRST_MERGE, MergePlane.UNKNOWN -> MergePlane.X_FIRST_MERGE
                        MergePlane.Y_FIRST_MERGE -> null
                    }
                    return when (getState(b)) {
                        MergePlane.Y_FIRST_MERGE, MergePlane.UNKNOWN -> MergePlane.Y_FIRST_MERGE
                        MergePlane.X_FIRST_MERGE -> null
                    }
                    when (getState(b)) {
                        MergePlane.X_FIRST_MERGE -> MergePlane.X_FIRST_MERGE
                        MergePlane.Y_FIRST_MERGE -> MergePlane.Y_FIRST_MERGE
                        MergePlane.UNKNOWN -> {
                            val ld = a.getLink(b)
                            if (ld != null && ld.direction != null) {
                                return when (ld.direction) {
                                    Direction.UP, Direction.DOWN -> MergePlane.Y_FIRST_MERGE
                                    Direction.LEFT, Direction.RIGHT -> MergePlane.X_FIRST_MERGE
                                }
                            }
                            MergePlane.UNKNOWN
                        }
                    }
                }
                MergePlane.Y_FIRST_MERGE -> {
                    return when (getState(b)) {
                        MergePlane.Y_FIRST_MERGE, MergePlane.UNKNOWN -> MergePlane.Y_FIRST_MERGE
                        MergePlane.X_FIRST_MERGE -> null
                    }
                    when (getState(b)) {
                        MergePlane.X_FIRST_MERGE -> MergePlane.X_FIRST_MERGE
                        MergePlane.Y_FIRST_MERGE -> MergePlane.Y_FIRST_MERGE
                        MergePlane.UNKNOWN -> {
                            val ld = a.getLink(b)
                            if (ld != null && ld.direction != null) {
                                return when (ld.direction) {
                                    Direction.UP, Direction.DOWN -> MergePlane.Y_FIRST_MERGE
                                    Direction.LEFT, Direction.RIGHT -> MergePlane.X_FIRST_MERGE
                                }
                            }
                            MergePlane.UNKNOWN
                        }
                    }
                }
                MergePlane.UNKNOWN -> when (getState(b)) {
                    MergePlane.X_FIRST_MERGE -> MergePlane.X_FIRST_MERGE
                    MergePlane.Y_FIRST_MERGE -> MergePlane.Y_FIRST_MERGE
                    MergePlane.UNKNOWN -> {
                        val ld = a.getLink(b)
                        if (ld != null && ld.direction != null) {
                            return when (ld.direction) {
                                Direction.UP, Direction.DOWN -> MergePlane.Y_FIRST_MERGE
                                Direction.LEFT, Direction.RIGHT -> MergePlane.X_FIRST_MERGE
                            }
                        }
                        MergePlane.UNKNOWN
                    }
                }
            }
            throw LogicException("Eventuality not considered: " + getType(a) + " " + getType(b))
        }

        /**
         * Only allows the merge if the neighbour is in the right state
         * @param l
         */
		@JvmStatic
		fun compatibleNeighbour(originatingGroup: GroupPhase.Group, destinationGroup: GroupPhase.Group): Boolean {
            return getMergePlane(originatingGroup, destinationGroup) != null
        }

        fun inState(group: GroupPhase.Group, vararg okStates: Any): Boolean {
            for (i in 0 until okStates.size) {
                if (getState(group) == okStates[i]) {
                    return true
                }
            }
            return false
        }

        @JvmStatic
		fun getState(group: GroupPhase.Group): MergePlane {
            return (group.type as DirectedGroupAxis).state
        }

        fun getLayoutFor(`in`: GroupPhase.Group, g: GroupPhase.Group?): Layout? {
            val ax = getType(`in`)
            return if (`in`.layout == null) {
                null
            } else when (`in`.layout) {
                Layout.LEFT, Layout.RIGHT -> if (ax.isHorizontal) layoutSide(`in`, g) else null
                Layout.UP, Layout.DOWN -> if (ax.isVertical) layoutSide(`in`, g) else null
                else ->            //HORIZONTAL, VERTICAL:
                    `in`.layout
            }
        }

        private fun layoutSide(`in`: GroupPhase.Group, g: GroupPhase.Group?): Layout {
            val cg = `in` as CompoundGroup
            return if (cg.b === g) {
                cg.layout
            } else if (cg.a === g) {
                Layout.reverse(cg.layout)!!
            } else {
                throw LogicException()
            }
        }

        @JvmStatic
		fun getType(g: GroupPhase.Group): DirectedGroupAxis {
            return g.axis as DirectedGroupAxis
        }
    }
}