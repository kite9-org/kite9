package org.kite9.diagram.visualization.planarization.rhd.layout

import org.kite9.diagram.common.algorithms.det.UnorderedSet
import org.kite9.diagram.common.algorithms.ssp.PriorityQueue
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.group.DirectedGroupAxis
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkDetail
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkProcessor
import kotlin.collections.plusAssign

/**
 * Modifies the queue so that we layout the groups with the most disparate group of links first.
 * This is based on a count of the number of groups that are already placed that the candidate
 * groups link to.
 */
class MostNetworkedFirstLayoutQueue(size: Int) : LayoutQueue, Logable {
    var log = Kite9Log.instance(this)

    data class NetworkedItem(val group: CompoundGroup, val size: Int, val isAxisAligned: Boolean, val horizontal: Boolean, val vertical: Boolean, val decided: Boolean) {
        override fun toString(): String {
            return "NI: ${group.groupNumber} size=$size axis=$isAxisAligned h=$horizontal v=$vertical decided=${decided} g=$group"
        }
    }

    fun liveLinkCount(g: CompoundGroup) : Int {
        var liveGroupLinkCount = 0
        val lm = g.linkManager
        log.send(if (log.go()) null else "Counting Network size for " + g.groupNumber)
        val links = lm.subset(lm.allMask())
        for (ld in links) {
            liveGroupLinkCount += ld.numberOfLinks.toInt()
        }
        return liveGroupLinkCount
    }

    override fun offer(item: CompoundGroup) {
        val axis = item.axis as DirectedGroupAxis
        val decided = Layout.toDirection(item.getLayout()) != null
        val ni = NetworkedItem(item, liveLinkCount(item), axis.isAxisAligned, axis.isHorizontal, axis.isVertical, decided)
        todo.add(ni)
        log.send("Created: $ni")
    }

    private fun countLinkNetworkSize(ld: LinkDetail): Int {
        // drill down looking for first ready group
        val out = intArrayOf(0)
        ld.processToLevel(
                object : LinkProcessor {
                    override fun process(
                            originatingGroup: Group,
                            destinationGroup: Group,
                            ld: LinkDetail
                    ) {
                        out[0] += countLinkNetworkSize(ld)
                    }
                },
                1
        )
        return out[0]
    }

    private fun mergesAxis(g: CompoundGroup) : Boolean {
        val axis = g.axis as DirectedGroupAxis
        return !axis.isHorizontal && !axis.isVertical
    }

    var todo: PriorityQueue<NetworkedItem> =
            PriorityQueue(
                    size,
                    Comparator { arg0, arg1 ->

                        /**
                         * First, in axis groups get priority over others
                         */
                        if (arg0.isAxisAligned != arg1.isAxisAligned) {
                            return@Comparator -arg0.isAxisAligned.compareTo(arg1.isAxisAligned)
                        }

                        /**
                         * Then let's process any that are merging axes together
                         */
                        if (mergesAxis(arg0.group) != mergesAxis(arg1.group)) {
                            return@Comparator -mergesAxis(arg0.group).compareTo(mergesAxis(arg1.group))
                        }

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

                        // lowest number first (reflects merge ordering)
                        val a0n = arg0.group.groupNumber
                        val a1n = arg1.group.groupNumber
                        a0n.compareTo(a1n)
                    }
            )

    override fun poll(): Group? {
        while (todo.size() > 0) {
            val nw = todo.remove()!!
            val out = nw.group
            return out
        }
        return null
    }

    /** Updates (by creating new NetworkedItems) the groups currently in todo. */
    override fun complete(item: CompoundGroup) {
    }

    override val prefix: String
        get() = "MNFQ"
    override val isLoggingEnabled: Boolean
        get() = true
}
