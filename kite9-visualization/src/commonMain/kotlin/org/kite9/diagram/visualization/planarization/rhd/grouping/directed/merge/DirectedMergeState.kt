package org.kite9.diagram.visualization.planarization.rhd.grouping.directed.merge

import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.merge.Containers
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.merge.BasicMergeState
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.ContainerMergeType
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.group.DirectedGroupAxis.Companion.compatibleNeighbour
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.group.DirectedGroupAxis.Companion.getState
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.group.DirectedLinkManager.Companion.createMask
import org.kite9.diagram.visualization.planarization.rhd.links.ContradictionHandler
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkDetail

open class DirectedMergeState(ch: ContradictionHandler, elements: Int) :
        BasicMergeState(ch, elements) {

    override fun isLinkCounted(from: Group?, to: Group?, via: Group, ld: LinkDetail?): Boolean {
        return if (compatibleNeighbour(from!!, via)) {
            super.isLinkCounted(from, to, via, ld)
        } else {
            false
        }
    }

    fun getContainerMergeType(a: Group, b: Group): ContainerMergeType {
        val common = Containers.hasCommonLiveContainer(this, a, b)
        val increases = Containers.increasesContainers(this, a, b)
        return if (common) {
            if (increases) ContainerMergeType.JOINING_EXTRA_CONTAINERS
            else ContainerMergeType.WITHIN_LIVE_CONTAINER
        } else {
            ContainerMergeType.NO_LIVE_CONTAINER
        }
    }

    class ShapeIndex(private val g: Group) {
        override fun equals(other: Any?): Boolean {
            return if (other is ShapeIndex) {
                // first, check for group equality.
                if (g === other.g) {
                    true
                } else {
                    // otherwise, see if the groups are equal because they contain the same leaves.
                    g.leafList == other.g.leafList
                }
            } else {
                false
            }
        }

        override fun hashCode(): Int {
            return g.hashCode()
        }
    }

    private val noDirectedMergeNeeded // live groups based on a key of which leaf groups they
    // contain
    : MutableMap<ShapeIndex, Group> =
            HashMap(elements)

    fun completedDirectionalMerge(combined: Group): Boolean {
        val mp = getState(combined)
        val mask =
                createMask(
                        mp,
                        false,
                        false,
                        Direction.UP,
                        Direction.DOWN,
                        Direction.LEFT,
                        Direction.RIGHT
                )
        val incomplete = combined.linkManager.subset(mask)
        return incomplete.size == 0
    }

    override fun removeLiveGroup(a: Group) {
        super.removeLiveGroup(a)
        if (completedDirectionalMerge(a)) {
            val i = ShapeIndex(a)
            val b = noDirectedMergeNeeded[i]
            if (b === a) {
                noDirectedMergeNeeded.remove(i)
            }
        }
    }

    fun getCompoundGroupWithSameContents(g: Group): Group? {
        val toMatch = ShapeIndex(g)
        return noDirectedMergeNeeded[toMatch]
    }

    override fun addLiveGroup(group: Group) {
        super.addLiveGroup(group)
        if (completedDirectionalMerge(group)) {
            val i = ShapeIndex(group)
            val existing = noDirectedMergeNeeded[i]
            if (existing == null) {
                noDirectedMergeNeeded[i] = group
            }
        }
    }
}
