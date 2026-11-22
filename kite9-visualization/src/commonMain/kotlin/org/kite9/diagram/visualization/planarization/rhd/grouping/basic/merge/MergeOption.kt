package org.kite9.diagram.visualization.planarization.rhd.grouping.basic.merge

import kotlin.math.abs
import kotlin.math.round
import org.kite9.diagram.common.hints.planarizationDistance
import org.kite9.diagram.common.hints.positionDistance
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group

/**
 * Holds details about a potential merge. Best merges are done first. Because merge options are used
 * in priority queues, they should not be altered while the object is in the queue.
 */
class MergeOption(
        a: Group,
        b: Group,
        val number: Int,
        var p: Int,
        var alignedGroup: Group?,
        var alignedDirection: Direction?
) : Comparable<MergeOption> {
    var mk = MergeKey(a, b)

    private var alignmentGroupSize = alignedGroup?.size ?: Int.MAX_VALUE
    private var totalLinks = 0f
    private var ordinalDistance = Int.MAX_VALUE
    private var linkRank = 0
    private var linksIncluded = 0f
    private var linksAligned = 0f
    private var planarDistance: Float? = null
    private var renderedDistance: Float? = null
    private var size =
            mk.a.size + mk.b.size // size of the groups, in terms of contained items subsumed

    /** Higher numbers indicate worse priority. 100 or more is illegal. */
    var priority = p
        private set

    /**
     * WARNING: this should only be called if the merge option has been removed from the merge
     * state.
     */
    fun resetPriority(p: Int) {
        priority = p
    }

    val mergeType: MergeType
        get() =
                if (linksIncluded >= GroupPhase.LINK_WEIGHT) {
                    MergeType.LINKED
                } else if (linksAligned > 0) {
                    MergeType.ALIGNED
                } else {
                    MergeType.NEIGHBOUR
                }

    override fun compareTo(other: MergeOption): Int {
        // order by priority first
        if (priority != other.priority) {
            return priority.compareTo(other.priority)
        }

        // order by merge type first
        if (mergeType !== other.mergeType) {
            return mergeType.ordinal.compareTo(other.mergeType.ordinal)
        }
        when (mergeType) {
            MergeType.LINKED -> {
                // respect highest ranked links first.
                if (other.linkRank != linkRank) {
                    return -linkRank.compareTo(other.linkRank)
                }

                // we need t combine lowest-level stuff first
                if (other.size != size) {
                    return size.compareTo(other.size)
                }

                // most value is from reducing total external links because they become internal
                if (other.linksIncluded != linksIncluded) {
                    return -linksIncluded.compareTo(other.linksIncluded)
                }

                // finally, try to avoid merging two "hub" neighbour attr together
                if (totalLinks != other.totalLinks) {
                    return totalLinks.compareTo(other.totalLinks)
                }
            }
            MergeType.ALIGNED -> {
                // join groups with smallest alignment group size (i.e. the group they both link to
                // is smallest)
                if (alignmentGroupSize != other.alignmentGroupSize) {
                    return alignmentGroupSize.compareTo(other.alignmentGroupSize)
                }

                // aligning links also reduces complexity of the overall graph, but not as much
                if (linksAligned != other.linksAligned) {
                    return -linksAligned.compareTo(other.linksAligned)
                }

                // leave group with least non-aligned links
                if (totalLinks - linksAligned != other.totalLinks - linksAligned) {
                    return (totalLinks - linksAligned).compareTo(
                            other.totalLinks - other.linksAligned
                    )
                }

                // we need to combine lowest-level stuff first
                if (other.size != size) {
                    return size.compareTo(other.size)
                }
            }
            else -> {
                // merge together neighbours with least chance of being moved from the outside
                val thistl = round(totalLinks)
                val arg0tl = round(other.totalLinks)
                if (thistl != arg0tl) {
                    return thistl.compareTo(arg0tl)
                }

                // merge closest neighbours first, to respect the ordering in the
                // xml
                if (ordinalDistance != other.ordinalDistance) {
                    return ordinalDistance.compareTo(other.ordinalDistance)
                }

                // try and merge smallest first, to achieve b-tree
                // and also to allow for more buddy merging
                if (other.size != size) {
                    return size.compareTo(other.size)
                }
            }
        }
        val dc = distanceCompare(this, other)
        return if (dc != 0) {
            dc
        } else number.compareTo(other.number)
    }

    private fun distanceCompare(a: MergeOption, b: MergeOption): Int {
        return if (a.planarDistance != null &&
                        b.planarDistance != null &&
                        a.planarDistance != b.planarDistance
        ) {
            a.planarDistance!!.compareTo(b.planarDistance!!)
        } else if (a.renderedDistance != null &&
                        b.renderedDistance != null &&
                        a.renderedDistance != b.renderedDistance
        ) {
            a.renderedDistance!!.compareTo(b.renderedDistance!!)
        } else {
            0
        }
    }

    override fun toString(): String {
        return ("[MO: " +
                number +
                " " +
                mk.a.groupNumber +
                "(" +
                mk.a.size +
                ")  " +
                mk.b.groupNumber +
                "(" +
                mk.b.size +
                "): t= " +
                mergeType +
                " i=" +
                linksIncluded +
                " a=" +
                linksAligned +
                " t=" +
                totalLinks +
                "ags=" +
                alignmentGroupSize +
                " od=" +
                ordinalDistance +
                ", p=" +
                priority +
                " a=" +
                alignedGroup +
                " ad=" +
                alignedDirection +
                " lr=" +
                linkRank +
                "]")
    }

    /** Call this function on a merge option to figure out it's priority. */
    fun calculateMergeOptionMetrics(ms: BasicMergeState) {
        totalLinks = 0f
        linksAligned = 0f
        linksIncluded = 0f
        linkRank = 0
        val a = mk.a
        val b = mk.b
        linkCount(a, b)
        linkCount(b, a)
        size = a.size + b.size
        ordinalDistance = abs(a.groupOrdinal - b.groupOrdinal)
        planarDistance = planarizationDistance(a.hints, b.hints)
        renderedDistance = positionDistance(a.hints, b.hints)
    }

    private fun linkCount(group: Group, cand: Group) {
        val ldC = group.getLink(cand)
        if (ldC != null) {
            linksIncluded += ldC.numberOfLinks / 2f
            if (ldC.direction != null) {
                linkRank = ldC.linkRank
            }
        }
        totalLinks += group.linkManager.linkCount.toFloat()
        if (alignedGroup != null) {
            val ldA = group.getLink(alignedGroup!!)
            if (ldA != null) {
                linksAligned += ldA.numberOfLinks
            }
        }
    }
}
