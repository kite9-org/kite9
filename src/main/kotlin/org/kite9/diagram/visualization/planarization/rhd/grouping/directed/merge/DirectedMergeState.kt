package org.kite9.diagram.visualization.planarization.rhd.grouping.directed.merge

import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.merge.BasicMergeState
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.ContainerMergeType
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.group.DirectedGroupAxis.Companion.compatibleNeighbour
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.group.DirectedGroupAxis.Companion.getState
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.group.DirectedLinkManager.Companion.createMask
import org.kite9.diagram.visualization.planarization.rhd.links.ContradictionHandler
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkDetail
import java.util.*

open class DirectedMergeState(ch: ContradictionHandler) : BasicMergeState(ch) {

    override fun isLinkCounted(from: Group?, to: Group?, via: Group, ld: LinkDetail?): Boolean {
        return if (compatibleNeighbour(from!!, via)) {
            super.isLinkCounted(from, to, via, ld)
        } else {
            false
        }
    }

    fun getContainerMergeType(a: Group, b: Group): ContainerMergeType {
        val common = hasCommonLiveContainer(a, b)
        val increases = increasesContainers(a, b)
        return if (common) {
            if (increases) ContainerMergeType.JOINING_EXTRA_CONTAINERS else ContainerMergeType.WITHIN_LIVE_CONTAINER
        } else {
            ContainerMergeType.NO_LIVE_CONTAINER
        }
    }

    private fun hasCommonLiveContainer(a: Group, b: Group): Boolean {
        val ac = getContainersFor(a)!!.keys
        val bc = getContainersFor(b)!!.keys
        val itc = if (ac.size < bc.size) ac else bc
        val inc = if (ac.size < bc.size) bc else ac
        for (container in itc) {
            if (inc.contains(container) && isContainerLive(container)) return true
        }
        return false
    }

    /**
     * If the resulting group is going to end up with more live containers than a or b individually,
     * return true.
     */
    fun increasesContainers(
        a: Group?,
        b: Group?
    ): Boolean {
        val ac: Map<Container, GroupContainerState?>? = getContainersFor(a)
        val bc: Map<Container, GroupContainerState?>? = getContainersFor(b)
        return hasDifferentContainers(ac, bc) && hasDifferentContainers(bc, ac)
    }

    private fun hasDifferentContainers(
        ac: Map<Container, GroupContainerState?>?,
        bc: Map<Container, GroupContainerState?>?
    ): Boolean {
        for (container in ac!!.keys) {
            if (bc!![container] == null) {
                return true
            }
        }
        return false
    }

    class ShapeIndex(private val g: Group) {
        override fun equals(arg0: Any?): Boolean {
            return if (arg0 is ShapeIndex) {
                // first, check for group equality.
                if (g === arg0.g) {
                    true
                } else {
                    // otherwise, see if the groups are equal because they contain the same leaves.
                    g.leafList == arg0.g.leafList
                }
            } else {
                false
            }
        }

        override fun hashCode(): Int {
            return g.hashCode()
        }
    }

    private var noDirectedMergeNeeded // live groups based on a key of which leaf groups they contain
            : MutableMap<ShapeIndex, Group>? = null

    fun completedDirectionalMerge(combined: Group): Boolean {
        val mp = getState(combined)
        val mask = createMask(mp, false, false, Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT)
        val incomplete = combined.linkManager.subset(mask)
        return incomplete.size == 0
    }

    override fun initialise(capacity: Int, containers: Int, log: Kite9Log?) {
        super.initialise(capacity, containers, log)
        noDirectedMergeNeeded = HashMap(capacity)
    }

    override fun removeLiveGroup(a: Group) {
        super.removeLiveGroup(a)
        if (completedDirectionalMerge(a)) {
            val i = ShapeIndex(a)
            val b = noDirectedMergeNeeded!![i]
            if (b === a) {
                noDirectedMergeNeeded!!.remove(i)
            }
        }
    }

    fun getCompoundGroupWithSameContents(g: Group): Group? {
        val toMatch = ShapeIndex(g)
        return noDirectedMergeNeeded!![toMatch]
    }

    override fun addLiveGroup(group: Group) {
        super.addLiveGroup(group)
        if (completedDirectionalMerge(group)) {
            val i = ShapeIndex(group)
            val existing = noDirectedMergeNeeded!![i]
            if (existing == null) {
                noDirectedMergeNeeded!![i] = group
            }
        }
    }
}