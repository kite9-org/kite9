package org.kite9.diagram.visualization.planarization.rhd.layout

import org.kite9.diagram.common.algorithms.det.UnorderedSet
import org.kite9.diagram.common.algorithms.ssp.PriorityQueue
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkDetail
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkProcessor

/**
 * Modifies the queue so that we layout the groups with the most disparate group of links first.
 * This is based on a count of the number of groups that are already placed that the candidate groups link to.
 */
class MostNetworkedFirstLayoutQueue(size: Int) : LayoutQueue, Logable {
    var log = Kite9Log(this)

    data class NetworkedItem(val group: GroupPhase.Group, val size: Int) {
        override fun toString(): String {
            return "NI: " + group.groupNumber + " size = " + size
        }
    }

    override fun offer(item: GroupPhase.Group) {
        if (canLayout(item)) {
            var liveGroupLinkCount = 0
            val lm = item.linkManager
            log.send(if (log.go()) null else "Counting Network size for " + item.groupNumber)
            val links = lm.subset(lm.allMask())
            for (ld in links) {
                liveGroupLinkCount += countLinkNetworkSize(ld)
            }
            networkSizes[item] = liveGroupLinkCount
            val ni = NetworkedItem(item, liveGroupLinkCount)
            todo.add(ni)
            log.send("Created: $ni")
        }
    }

    private fun canLayout(item: GroupPhase.Group?): Boolean {
        return item!!.getAxis().isReadyToPosition(completedGroups)
    }

    private fun countLinkNetworkSize(ld: LinkDetail): Int {
        val complete = completedGroups.contains(ld.group)
        return if (complete) {
            // drill down looking for first ready group
            val out = intArrayOf(0)
            ld.processToLevel(object : LinkProcessor {
                override fun process(
                    originatingGroup: GroupPhase.Group,
                    destinationGroup: GroupPhase.Group,
                    ld2: LinkDetail
                ) {
                    out[0] += countLinkNetworkSize(ld2!!)
                }
            }, 1)
            out[0]
        } else {
            log.send(if (log.go()) null else " -- link to " + ld.group)
            1
        }
    }

    var todo: PriorityQueue<NetworkedItem> = PriorityQueue(size, Comparator { arg0, arg1 ->

        /**
         * Although priority is top down, within a given level, do groups in the same order than they were merged
         * in. This means that we do "hub" groups before "edge" ones.
         */
        // most networked first
        var a0d = arg0.size
        var a1d = arg1.size
        if (a0d != a1d) {
            return@Comparator -a0d.compareTo(a1d)
        }

        // largest first
        a0d = arg0.group.size
        a1d = arg1.group.size
        if (a0d != a1d) {
            return@Comparator -a0d.compareTo(a1d)
        }

        // lowest number first
        val a0n = arg0.group.groupNumber
        val a1n = arg1.group.groupNumber
        a0n.compareTo(a1n)
    })
    var networkSizes: MutableMap<GroupPhase.Group, Int> = HashMap(size * 2)
    var completedGroups: MutableSet<GroupPhase.Group> = UnorderedSet(size * 2)
    override fun poll(): GroupPhase.Group? {
        while (todo.size > 0) {
            val nw = todo.remove()!!
            val out = nw.group
            networkSizes.remove(out)
            if (!completedGroups.contains(out)) {
                out.linkManager.linkCount = nw.size
                return out
            }
        }
        return null
    }

    /**
     * Updates (by creating new NetworkedItems) the groups currently in todo.
     */
    override fun complete(item: CompoundGroup) {
        completedGroups.add(item)
        val a = item.a
        val b = item.b
        val horiz = item.getAxis().isHorizontal
        val links = item.linkManager.subset(item.linkManager.allMask())
        for (ld in links) {
            val group = ld.group
            checkAndIncrementGroup(group, a, b, ld, horiz)
        }
    }

    private fun checkAndIncrementGroup(
        group: GroupPhase.Group,
        a: GroupPhase.Group,
        b: GroupPhase.Group,
        ld: LinkDetail,
        horiz: Boolean
    ) {
        var group: GroupPhase.Group? = group
        if (completedGroups.contains(group)) {
            if (group is CompoundGroup) {
                // need to work our way down to incomplete ones, no point updating complete groups
                ld.processToLevel(object : LinkProcessor {
                    override fun process(
                        originatingGroup: GroupPhase.Group,
                        destinationGroup: GroupPhase.Group,
                        ld2: LinkDetail
                    ) {
                        checkAndIncrementGroup(destinationGroup, a, b, ld2!!, horiz)
                    }
                }, 1)
            }
        } else {
            group = getGroupBeingLaidOut(group, horiz)
            if (group != null && ld.from(a) && ld.from(b)) {
                val existingLinks = safeGet(group)
                networkSizes[group] = existingLinks + 1
                val ni = NetworkedItem(group, existingLinks + 1)
                todo.add(ni)
                log.send("Bumped priority: $ni")
            }
        }
    }

    private fun getGroupBeingLaidOut(group: GroupPhase.Group?, horiz: Boolean): GroupPhase.Group? {
        var group = group
        while (!canLayout(group)) {
            if (completedGroups.contains(group)) {
                return null
            }
            group = group!!.getAxis().getParentGroup(horiz)
        }
        return group
    }

    private fun safeGet(group: GroupPhase.Group): Int {
        return networkSizes[group] ?: return 0
    }

    override val prefix: String
        get() = "MNFQ"
    override val isLoggingEnabled: Boolean
        get() = true

}