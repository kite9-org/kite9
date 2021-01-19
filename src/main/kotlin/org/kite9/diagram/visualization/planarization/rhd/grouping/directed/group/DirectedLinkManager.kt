package org.kite9.diagram.visualization.planarization.rhd.grouping.directed.group

import org.kite9.diagram.common.BiDirectional
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Direction.Companion.reverse
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.merge.BasicMergeState
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.MergePlane
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.group.DirectedGroupAxis.Companion.getMergePlane
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.group.DirectedGroupAxis.Companion.getState
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkDetail
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkProcessor
import java.util.*

class DirectedLinkManager(private val ms: BasicMergeState, private val g: Group) : LinkManager {

    val links = LinkedHashMap<Group, LinkDetail>()
    private val yFirstNearestNeighbours: MutableSet<Direction> = mutableSetOf()
    private val xFirstNearestNeighbours: MutableSet<Direction> = mutableSetOf()

    protected abstract class AbstractLinkDetail(
        override val direction: Direction?,
        override val group: Group?,
        val originatingGroup: Group?
    ) : LinkDetail {
        var inContainer = false
        var nearestNeighbour = false
        override fun from(b: Group): Boolean {
            return originatingGroup === b
        }

        override fun processToLevel(lp: LinkProcessor, l: Int) {
            lp.process(originatingGroup!!, group!!, this)
        }

        override fun processLowestLevel(lp: LinkProcessor) {
            processToLevel(lp, Int.MAX_VALUE)
        }

        override fun toString(): String {
            return numberOfLinks.toString() + "/d=" + direction + "/" + (if (isOrderingLink) "O" else "~O") + "/" + (if (inContainer) "C" else "~C") + "/" + (if (nearestNeighbour) "N" else "~N") + "/" + group!!.groupNumber
        }
    }

    protected class ActualLinkDetail(
        g: Group?,
        o: Group?,
        override val numberOfLinks: Float,
        d: Direction?,
        override var isOrderingLink: Boolean,
        override val linkRank: Int,
        c: Iterable<BiDirectional<Connected>>
    ) : AbstractLinkDetail(d, g, o) {
        override val connections: MutableList<BiDirectional<Connected>> = LinkedList()

        constructor(toCopy: ActualLinkDetail, lmsGroup: Group?) : this(
            toCopy.group,
            lmsGroup,
            toCopy.numberOfLinks,
            toCopy.direction,
            toCopy.isOrderingLink,
            toCopy.linkRank,
            toCopy.connections
        ) {
            inContainer = toCopy.inContainer
            nearestNeighbour = toCopy.nearestNeighbour
        }

        private fun addConnections(c: Iterable<BiDirectional<Connected>>) {
            for (item in c) {
                if (!connections.contains(c)) {
                    connections.add(item)
                }
            }
        }

        init {
            addConnections(c)
        }
    }

    private fun keepLinkTo(otherGroup: Group): Boolean {
        val mp = getMergePlane(g, otherGroup)
        return mp != null
    }

    /**
     * This is used where we want to inherit a link detail in a compound group from one of the
     * contained groups.
     */
    protected class WrapLinkDetail(var inside: LinkDetail, lmsGroup: Group?) : AbstractLinkDetail(
        inside.direction, inside.group, lmsGroup
    ) {
        override fun from(b: Group): Boolean {
            return super.from(b) || inside.from(b)
        }

        override val isOrderingLink: Boolean
            get() = inside.isOrderingLink
        override val numberOfLinks: Float
            get() = inside.numberOfLinks
        override val connections: Iterable<BiDirectional<Connected>>
            get() = inside.connections

        override fun processToLevel(lp: LinkProcessor, i: Int) {
            inside.processToLevel(lp, i)
        }

        override val linkRank: Int
            get() = inside.linkRank
    }

    protected inner class CompoundLinkDetail(cg: Group?, lmsGroup: Group?, a: LinkDetail?, b: LinkDetail?) :
        AbstractLinkDetail(
            ms.contradictionHandler.checkContradiction(a, b, null), cg, lmsGroup
        ) {
        var a: LinkDetail?
        var b: LinkDetail?
        override val isOrderingLink: Boolean
            get() = a!!.isOrderingLink || b!!.isOrderingLink
        override val numberOfLinks: Float
            get() = a!!.numberOfLinks + b!!.numberOfLinks
        override val connections: Iterable<BiDirectional<Connected>>
            get() = object : Iterable<BiDirectional<Connected>> {
                override fun iterator(): Iterator<BiDirectional<Connected>> {
                    return object : MutableIterator<BiDirectional<Connected>> {
                        var ai = a!!.connections.iterator()
                        var bi = b!!.connections.iterator()
                        override fun hasNext(): Boolean {
                            return ai.hasNext() || bi.hasNext()
                        }

                        override fun next(): BiDirectional<Connected> {
                            return if (ai.hasNext()) {
                                ai.next()
                            } else {
                                bi.next()
                            }
                        }

                        override fun remove() {
                            throw UnsupportedOperationException()
                        }
                    }
                }
            }

        override fun processToLevel(lp: LinkProcessor, i: Int) {
            if (i > 0) {
                (a as AbstractLinkDetail?)!!.processToLevel(lp, i - 1)
                (b as AbstractLinkDetail?)!!.processToLevel(lp, i - 1)
            } else {
                super.processToLevel(lp, i - 1)
            }
        }

        override fun from(g: Group): Boolean {
            return if (super.from(g)) {
                true
            } else {
                (a as AbstractLinkDetail?)!!.from(g) || (b as AbstractLinkDetail?)!!.from(g)
            }
        }

        override val linkRank: Int
            get() = Math.max(a!!.linkRank, b!!.linkRank)

        init {
            var a = a
            var b = b
            a = a ?: NULL
            b = b ?: NULL
            this.a = a
            this.b = b
            inContainer = (a as AbstractLinkDetail).inContainer || (b as AbstractLinkDetail) .inContainer
            nearestNeighbour = (a as AbstractLinkDetail).nearestNeighbour || (b as AbstractLinkDetail).nearestNeighbour
        }
    }

    fun getAxisDirection(mp: MergePlane, d: Direction?, incontainer: Boolean): Int {
        var mask = if (incontainer) 2048 else 0
        mask = if (mp === MergePlane.X_FIRST_MERGE) {
            mask + 1024
        } else if (mp === MergePlane.Y_FIRST_MERGE) {
            mask + 512
        } else {
            mask + 128
        }
        return if (d == null) {
            mask
        } else when (d) {
            Direction.UP -> mask + 1
            Direction.DOWN -> mask + 2
            Direction.LEFT -> mask + 3
            Direction.RIGHT -> mask + 4
            else -> mask + 4
        }
    }

    /**
     * Set if the nearest neighbour is single.
     */
    private val singleDirectedMergeOption: MutableMap<Int, Any?> = HashMap(10)
    fun getSingleDirectedMergeOption(
        l: Direction?,
        axis: MergePlane,
        ms: BasicMergeState?,
        inContainer: Boolean
    ): Group? {
        val ad = getAxisDirection(axis, l, inContainer)
        var out = singleDirectedMergeOption[ad]
        if (out == null) {
            //System.out.println("Recalc of SDM "+axis+" "+l+" "+inContainer+" group="+g.getGroupNumber());
            out = NONE
            val mask = createMask(axis, true, inContainer, l)
            val options = subsetGroup(mask)
            if (options.size == 1) {
                val first = options.iterator().next()
                val rmask = createMask(axis, true, inContainer, reverse(l))
                val lm = first.linkManager as DirectedLinkManager
                val reverse = lm.subsetGroup(rmask)
                if (reverse.size == 1 && reverse.contains(g)) {
                    out = first
                    lm.singleDirectedMergeOption[getAxisDirection(
                        axis,
                        reverse(l),
                        inContainer
                    )] = g
                }
            }
            singleDirectedMergeOption[ad] = out
        }
        return if (out !== NONE) {
            out as Group?
        } else {
            null
        }
    }

    fun sameContainer(`in`: Group?, ms: BasicMergeState): Boolean {
        var ac = containersFor(
            g, ms
        )
        var bc: Set<Container?>? = emptySet()
        if (ac == null) {
            // deal with containers not being set to start with
            ac = containersFor((g as CompoundGroup).a, ms)
            bc = containersFor(g.b, ms)
        }
        val itc = containersFor(`in`, ms)
        for (container in itc!!) {
            if (ac!!.contains(container)) {
                return true
            }
            if (bc!!.contains(container)) {
                return true
            }
        }
        return false
    }

    private fun ensureNearestNeighboursInitialised(mask: Int) {
        if (!masked(mask, MASK_NEAREST_NEIGHBOUR)) {
            return
        }
        if (masked(mask, MASK_X_FIRST)) {
            initialiseNearestNeighbourPlane(mask, MergePlane.X_FIRST_MERGE)
        }
        if (masked(mask, MASK_Y_FIRST)) {
            initialiseNearestNeighbourPlane(mask, MergePlane.Y_FIRST_MERGE)
        }
    }

    private fun initialiseNearestNeighbourPlane(mask: Int, mp: MergePlane) {
        if (masked(mask, MASK_UP)) initialiseNearestNeighbours(mp, Direction.UP)
        if (masked(mask, MASK_DOWN)) initialiseNearestNeighbours(mp, Direction.DOWN)
        if (masked(mask, MASK_LEFT)) initialiseNearestNeighbours(mp, Direction.LEFT)
        if (masked(mask, MASK_RIGHT)) initialiseNearestNeighbours(mp, Direction.RIGHT)
    }

    private fun initialiseNearestNeighbours(mp: MergePlane, d: Direction) {
        if (mp === MergePlane.X_FIRST_MERGE) {
            if (xFirstNearestNeighbours.contains(d)) {
                return
            }
        } else if (mp === MergePlane.Y_FIRST_MERGE) {
            if (yFirstNearestNeighbours.contains(d)) {
                return
            }
        } else {
            throw LogicException("Must be checking NN with a given axis")
        }
        val mask = createMask(mp, false, false, d)
        val myNeighbours = subset(mask)

        // set them all to true to start with
        for (linkDetail in myNeighbours) {
            (linkDetail as AbstractLinkDetail).nearestNeighbour = true
        }

        // narrow them down
        for (linkDetail in myNeighbours) {
            checkForExclusions(linkDetail.group!!, d, mp, 0, GroupChain(linkDetail.group!!))
        }
        if (mp === MergePlane.X_FIRST_MERGE) {
            xFirstNearestNeighbours.add(d)
        }
        if (mp === MergePlane.Y_FIRST_MERGE) {
            yFirstNearestNeighbours.add(d)
        }
    }

    private class GroupChain {
        var prev: GroupChain? = null
        var g: Group

        constructor(g: Group) {
            this.g = g
        }

        constructor(g: Group, prev: GroupChain?) {
            this.g = g
            this.prev = prev
        }

        fun stop(g: Group): Boolean {
            return this.g === g || if (prev != null) prev!!.stop(g) else false
        }
    }

    private fun checkForExclusions(group: Group, d: Direction, mp: MergePlane, level: Int, gc: GroupChain) {
        //System.out.println("Exc check: "+group+" level "+level);
        val mask = createMask(mp, false, false, d)
        val groupNeighbours = group.linkManager.subsetGroup(mask)
        for (g1 in groupNeighbours) {
            val ld = links[g1]
            if (ld != null) {
                if (!ld.isOrderingLink) {
                    // for ordering links, they are always neighbours (even when
                    // there is a contradiction)
                    (ld as AbstractLinkDetail).nearestNeighbour = false
                }
            } else {
                if (!gc.stop(g1)) {
                    checkForExclusions(g1, d, mp, level + 1, GroupChain(g1, gc))
                } else {
                    setContradiction(GroupChain(g1, gc), g1)
                }
            }
        }
    }

    private fun setContradiction(groupChain: GroupChain, stop: Group) {
        var groupChain: GroupChain? = groupChain
        var best = groupChain!!.g.getLink(groupChain.prev!!.g)
        var bestCount = Float.MAX_VALUE
        var ordering = best!!.isOrderingLink
        groupChain = groupChain.prev
        while (groupChain!!.g !== stop) {
            val current = groupChain!!.g.getLink(groupChain.prev!!.g)
            if (current!!.isOrderingLink == false && ordering == true ||
                current.numberOfLinks < bestCount
            ) {
                best = current
                bestCount = current.numberOfLinks
                ordering = false
            }
            groupChain = groupChain.prev
        }
        for (c in best!!.connections) {
            ms.contradictionHandler.setContradiction(c, false)
        }
    }

    override fun sortLink(
        d: Direction?,
        otherGroup: Group,
        linkValue: Float,
        ordering: Boolean,
        linkRank: Int,
        c: Iterable<BiDirectional<Connected>>
    ) {
        if (keepLinkTo(otherGroup)) {
            checkAddLinkDetail(d, otherGroup, linkValue, ordering, linkRank, c)
        }
    }

    override fun sortLink(ld: LinkDetail) {
        if (keepLinkTo(ld.group!!)) {
            checkAddLinkDetail(ld)
        }
    }

    private fun checkAddLinkDetail(other: LinkDetail) {
        val existing = links.remove(other.group)
        var cld: AbstractLinkDetail? = null
        cld = if (existing == null) {
            WrapLinkDetail(other, g)
        } else {
            CompoundLinkDetail(other.group, g, existing, other as AbstractLinkDetail)
        }
        cld.inContainer = sameContainer(other.group, ms)
        links[other.group!!] = cld
        linkCount += other.numberOfLinks.toInt()
    }

    private fun checkAddLinkDetail(
        d: Direction?, otherGroup: Group,
        linkValue: Float, ordering: Boolean, linkRank: Int,
        c: Iterable<BiDirectional<Connected>>
    ) {
        val ld = ActualLinkDetail(otherGroup, g, linkValue, d, ordering, linkRank, c)
        checkAddLinkDetail(ld)
    }

    abstract class FilterIterator<X>(links: Collection<Map.Entry<Group, LinkDetail>>, mask: Int) :
        MutableIterator<X> {
        var i: Iterator<Map.Entry<Group, LinkDetail>>
        var mask: Int
        var next: Map.Entry<Group, LinkDetail>? = null
        override fun hasNext(): Boolean {
            ensureNext()
            return next != null
        }

        protected fun ensureNext() {
            while (next == null && i.hasNext()) {
                next = i.next()
                if (!matches(next!!.value, mask)) {
                    next = null
                }
            }
        }

        override fun remove() {
            throw UnsupportedOperationException()
        }

        init {
            i = links.iterator()
            this.mask = mask
        }
    }

    class GroupFilterIterator(links: Collection<Map.Entry<Group, LinkDetail>>, mask: Int) :
        FilterIterator<Group>(links, mask) {
        override fun next(): Group {
            ensureNext()
            if (next == null) {
                throw NoSuchElementException()
            }
            val out = next!!.key
            next = null
            return out
        }
    }

    class LinkDetailFilterIterator(links: Collection<Map.Entry<Group, LinkDetail>>, mask: Int) :
        FilterIterator<LinkDetail>(links, mask) {
        override fun next(): LinkDetail {
            ensureNext()
            if (next == null) {
                throw NoSuchElementException()
            }
            val out: LinkDetail = next!!.value
            next = null
            return out
        }
    }

    abstract class AbstractDLMCollection<X> : AbstractCollection<X>() {
        var s = -1
        override val size by lazy {
            if (s == -1) {
                s = 0
                val iterator: Iterator<*> = this.iterator()
                while (iterator.hasNext()) {
                    iterator.next()
                    s++
                }
            }
            s
        }
    }

    override fun subset(mask: Int): Collection<LinkDetail> {
        ensureNearestNeighboursInitialised(mask)
        return object : AbstractDLMCollection<LinkDetail>() {
            override fun iterator(): MutableIterator<LinkDetail> {
                return LinkDetailFilterIterator(links.entries, mask)
            }
        }
    }

    override fun subsetGroup(mask: Int): Collection<Group> {
        ensureNearestNeighboursInitialised(mask)
        return object : AbstractDLMCollection<Group>() {
            override fun iterator(): MutableIterator<Group> {
                return GroupFilterIterator(links.entries, mask)
            }
        }
    }

    override fun allMask(): Int {
        return -1
    }

    override fun get(g: Group): LinkDetail? {
        return links[g]
    }

    override var linkCount = 0
    override fun notifyContainerChange(g: Group) {
        val ld = links[g]
        (ld as AbstractLinkDetail).inContainer = sameContainer(g, ms)
    }

    override fun notifyContainerChange() {
        for (ld in links.values) {
            (ld as AbstractLinkDetail).inContainer = sameContainer(ld.group, ms)
        }
    }

    override fun notifyMerge(g: CompoundGroup, aRemains: Boolean, bRemains: Boolean) {
        val aggregateDimension = getState(g)
        val aLD = links.remove(g.a)
        val bLD = links.remove(g.b)
        val cLD: AbstractLinkDetail = CompoundLinkDetail(g, this.g, aLD, bLD)
        when (aggregateDimension) {
            MergePlane.X_FIRST_MERGE -> {
                maybeKeepLink(g.a, aLD, aRemains, MergePlane.Y_FIRST_MERGE)
                maybeKeepLink(g.b, bLD, bRemains, MergePlane.Y_FIRST_MERGE)
                upgradeLink(g, cLD, MergePlane.X_FIRST_MERGE)
            }
            MergePlane.Y_FIRST_MERGE -> {
                maybeKeepLink(g.a, aLD, aRemains, MergePlane.X_FIRST_MERGE)
                maybeKeepLink(g.b, bLD, bRemains, MergePlane.X_FIRST_MERGE)
                upgradeLink(g, cLD, MergePlane.Y_FIRST_MERGE)
            }
            MergePlane.UNKNOWN -> {
                upgradeLink(g, cLD, MergePlane.Y_FIRST_MERGE)
                upgradeLink(g, cLD, MergePlane.X_FIRST_MERGE)
            }
        }
    }

    private fun upgradeLink(g: CompoundGroup, cLD: AbstractLinkDetail, plane: MergePlane) {
        if (keepLinkTo(g)) {
            links[g] = cLD

            // keep nearest neighbour up-to-date
            val d = cLD.direction
            if (d != null && cLD.nearestNeighbour) {
                checkNearestNeighbourInPlane(g, d, plane)
                //System.out.println("Clear SDM "+plane+" "+d+" group="+this.g.getGroupNumber());
                singleDirectedMergeOption.remove(getAxisDirection(plane, d, true))
                singleDirectedMergeOption.remove(getAxisDirection(plane, d, false))
            }
        }
    }

    private fun maybeKeepLink(oldGroup: Group, aLD: LinkDetail?, remains: Boolean, plane: MergePlane) {
        if (remains && aLD != null && keepLinkTo(oldGroup)) {
            links[oldGroup] = aLD
        }
    }

    private fun checkNearestNeighbourInPlane(g: Group, d: Direction, merge: MergePlane) {
        val mp = getState(this.g)
        if (mp.matches(merge)) {
            if (merge === MergePlane.X_FIRST_MERGE) {
                if (!xFirstNearestNeighbours.contains(d)) return
            } else if (merge === MergePlane.Y_FIRST_MERGE) {
                if (!yFirstNearestNeighbours.contains(d)) {
                    return
                }
            }
            checkForExclusions(g, d, merge, 0, GroupChain(g))
        }
    }

    override fun processAllLeavingLinks(compound: Boolean, mask: Int, lp: LinkProcessor) {
        for (ld in subset(mask)) {
            if (compound) {
                lp.process(g, ld.group!!, ld)
            } else {
                (ld as AbstractLinkDetail).processLowestLevel(lp)
            }
        }
    }

    override fun forLogging(): Map<Group, LinkDetail> {
        return links as Map<Group, LinkDetail>
    }

    override fun notifyAxisChange() {
        val iterator = links.keys.iterator()
        while (iterator.hasNext()) {
            val g = iterator.next()
            if (!keepLinkTo(g)) {
                iterator.remove()
            }
        }
    }

    companion object {
        const val MASK_X_FIRST = 1
        const val MASK_Y_FIRST = 2
        const val MASK_NEAREST_NEIGHBOUR = 4
        const val MASK_IN_CONTAINER = 8
        const val MASK_UP = 16
        const val MASK_DOWN = 32
        const val MASK_LEFT = 64
        const val MASK_RIGHT = 128
        const val MASK_NO_DIRECTION = 256
        @JvmStatic
		fun createMask(mp: MergePlane?, nearestNeighbour: Boolean, inContainer: Boolean, vararg d: Direction?): Int {
            var outMask = 0
            if (mp === MergePlane.X_FIRST_MERGE || mp === MergePlane.UNKNOWN || mp == null) {
                outMask += MASK_X_FIRST
            }
            if (mp === MergePlane.Y_FIRST_MERGE || mp === MergePlane.UNKNOWN || mp == null) {
                outMask += MASK_Y_FIRST
            }
            if (nearestNeighbour) {
                outMask += MASK_NEAREST_NEIGHBOUR
            }
            if (inContainer) {
                outMask += MASK_IN_CONTAINER
            }
            for (direction in d) {
                outMask += if (direction == null) {
                    MASK_NO_DIRECTION
                } else {
                    when (direction) {
                        Direction.UP -> MASK_UP
                        Direction.DOWN -> MASK_DOWN
                        Direction.LEFT -> MASK_LEFT
                        Direction.RIGHT -> MASK_RIGHT
                    }
                }
            }
            return outMask
        }

        val NULL: LinkDetail = object : AbstractLinkDetail(null, null, null) {
            override val isOrderingLink: Boolean
                get() = false
            override val numberOfLinks: Float
                get() = 0F
            override val connections: Iterable<BiDirectional<Connected>>
                get() = emptyList()

            override fun processToLevel(lp: LinkProcessor, i: Int) {}
            override fun from(b: Group): Boolean {
                return false
            }

            override val linkRank: Int
                get() = 0
        }

        private val NONE = Any()
        private fun containersFor(
            a: Group?,
            ms: BasicMergeState
        ): Set<Container?>? {
            val cf =
                ms.getContainersFor(a)
            return cf?.keys
        }

        fun matches(ld: LinkDetail, mask: Int): Boolean {
            if (mask == -1) {
                return true
            }

            // plane mask
            var planeOK = false
            if (masked(mask, MASK_X_FIRST)) {
                if (MergePlane.X_FIRST_MERGE.matches(getState(ld.group!!))) {
                    planeOK = true
                }
            }
            if (masked(mask, MASK_Y_FIRST)) {
                if (MergePlane.Y_FIRST_MERGE.matches(getState(ld.group!!))) {
                    planeOK = true
                }
            }
            if (!planeOK) {
                return false
            }

            // nearest neighbour
            if (masked(mask, MASK_NEAREST_NEIGHBOUR)) {
                if (!(ld as AbstractLinkDetail).nearestNeighbour) {
                    return false
                }
            }

            // in container
            if (masked(mask, MASK_IN_CONTAINER)) {
                if (!(ld as AbstractLinkDetail).inContainer) {
                    return false
                }
            }

            // direction mask
            var directionOK = false
            if (masked(mask, MASK_NO_DIRECTION)) {
                if (ld.direction == null) {
                    directionOK = true
                }
            }
            if (masked(mask, MASK_LEFT)) {
                if (ld.direction === Direction.LEFT) {
                    directionOK = true
                }
            }
            if (masked(mask, MASK_RIGHT)) {
                if (ld.direction === Direction.RIGHT) {
                    directionOK = true
                }
            }
            if (masked(mask, MASK_UP)) {
                if (ld.direction === Direction.UP) {
                    directionOK = true
                }
            }
            if (masked(mask, MASK_DOWN)) {
                if (ld.direction === Direction.DOWN) {
                    directionOK = true
                }
            }
            return if (!directionOK) {
                false
            } else true
        }

        private fun masked(mask: Int, m: Int): Boolean {
            return if (mask == -1) {
                false
            } else mask and m > 0
        }

        fun all(): Int {
            return -1
        }
    }
}