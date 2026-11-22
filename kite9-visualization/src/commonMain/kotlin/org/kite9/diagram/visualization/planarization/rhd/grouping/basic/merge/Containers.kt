package org.kite9.diagram.visualization.planarization.rhd.grouping.basic.merge

import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group

class Containers {

    companion object {

        fun getCommonContainers(ms: BasicMergeState, a: Group, b: Group): Set<Rectangular> {
            var ac = ms.getContainersFor(a)?.keys ?: emptySet()
            var bc = ms.getContainersFor(b)?.keys ?: emptySet()
            while (true) {
                if (ac.isEmpty() || bc.isEmpty())
                    throw LogicException("Group has no containers?")

                val intersect = ac.intersect(bc)

                if (intersect.isNotEmpty()) {
                    return intersect
                }

                ac = ac + ac.mapNotNull { it.getContainer() }.toSet()
                bc = bc + bc.mapNotNull { it.getContainer() }.toSet()
            }
        }


        /**
         * Attempts to find live containers shared by both a and b.
         */
        fun getCommonLiveContainers(ms: BasicMergeState, a: Group, b: Group): Set<Rectangular> {
            val cc = getCommonContainers(ms, a, b)
            return getLiveContainers(ms, cc)
        }

        fun getLiveContainers(ms: BasicMergeState, cc: Set<Rectangular>) : Set<Rectangular> {
            val live = cc.filter { ms.isContainerLive(it) }
            return live.toSet()
        }

        fun hasCommonLiveContainer(ms: BasicMergeState, a: Group, b: Group): Boolean {
            return getCommonLiveContainers(ms, a, b).isNotEmpty()
        }


        /**
         * If the resulting group is going to end up with more live containers than a or b individually,
         * return true.
         */
        fun increasesContainers(ms: BasicMergeState, a: Group?, b: Group?): Boolean {
            val ac: Map<Rectangular, BasicMergeState.GroupContainerState?>? = ms.getContainersFor(a)
            val bc: Map<Rectangular, BasicMergeState.GroupContainerState?>? = ms.getContainersFor(b)
            return hasDifferentContainers(ac, bc) && hasDifferentContainers(bc, ac)
        }

        private fun hasDifferentContainers(
            ac: Map<Rectangular, BasicMergeState.GroupContainerState?>?,
            bc: Map<Rectangular, BasicMergeState.GroupContainerState?>?
        ): Boolean {
            for (container in ac!!.keys) {
                if (bc!![container] == null) {
                    return true
                }
            }
            return false
        }

    }
}