package org.kite9.diagram.visualization.planarization.rhd

import org.kite9.diagram.common.BiDirectional
import org.kite9.diagram.common.algorithms.det.Deterministic
import org.kite9.diagram.common.algorithms.det.UnorderedSet
import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.common.elements.grid.GridPositioner
import org.kite9.diagram.common.elements.mapping.ElementMapper
import org.kite9.diagram.common.hints.PositioningHints.merge
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.*
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Direction.Companion.reverse
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.visualization.planarization.Tools.Companion.isConnectionContradicting
import org.kite9.diagram.visualization.planarization.Tools.Companion.isConnectionRendered
import org.kite9.diagram.visualization.planarization.rhd.links.ContradictionHandler
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkDetail
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkProcessor
import org.kite9.diagram.visualization.planarization.rhd.links.OrderingTemporaryBiDirectional
import kotlin.random.Random


/**
 * A GroupPhase is responsible for creating the Group data structures out of the
 * vertices, and holding various related lookup maps.
 *
 * @author robmoffat
 */
class GroupPhase(
    log: Kite9Log,
    top: DiagramElement,
    elements: Int,
    ab: GroupBuilder,
    ch: ContradictionHandler,
    gp: GridPositioner,
    em: ElementMapper
) {

    @JvmField
    var allGroups: MutableSet<LeafGroup>
    //private val hashCodeGenerator: Random
    private val pMap: MutableMap<Connected, LeafGroup>
    private val ab: GroupBuilder
    private val allLinks: MutableSet<Connection> = UnorderedSet(1000)
    private val ch: ContradictionHandler
    private val gp: GridPositioner
    private val em: ElementMapper
    private val hashCodeGenerator : Random

    fun getLeafGroupFor(ord: Connected): LeafGroup? {
        return pMap[ord]
    }

    /**
     * Creates leaf groups and any ordering between them, recursively.
     * @param ord  The object to create the group for
     * @param prev1 Previous element in the container
     * @param pMap Map of Connected to LeafGroups (created)
     */
    private fun createLeafGroup(
        ord: Connected,
        prev1: Connected?,
        pMap: MutableMap<Connected, LeafGroup>
    ): LeafGroup? {
        if (pMap[ord] != null) {
            throw LogicException("Diagram Element $ord appears multiple times in the diagram definition")
        }
        val cnr = ord.getContainer()
        val leaf = needsLeafGroup(ord)
        var g: LeafGroup? = null
        if (leaf) {
            g = LeafGroup(ord, cnr, ab.createAxis(), ab.createLinkManager())
            pMap[ord] = g
            allGroups.add(g)
        }
        if (prev1 != null && prev1 !== ord) {
            addContainerOrderingInfo(ord, prev1, cnr, null)
        }
        if (!leaf) {
            val l = (ord as Container).getLayout()
            if (l === Layout.GRID) {
                // need to iterate in 2d
                val grid = gp.placeOnGrid((ord as Container), false)

                // create unconnected groups
                val gridGroups: MutableMap<DiagramElement, LeafGroup> = LinkedHashMap()
                for (y in grid.indices) {
                    for (x in grid[0].indices) {
                        val de = grid[y][x]
                        if (!gridGroups.containsKey(de)) {
                            var gg = createLeafGroup(de as Connected, null, pMap)
                            if (gg == null) {
                                gg = getConnectionEnd(de)
                            }
                            gridGroups[de] = gg
                        }
                    }
                }

                // link them up
                for (y in grid.indices) {
                    for (x in grid[0].indices) {
                        val prevy = (if (y > 0) grid[y - 1][x] else null) as Connected?
                        val prevx = (if (x > 0) grid[y][x - 1] else null) as Connected?
                        val c = grid[y][x] as Connected
                        if (c !== prevx && prevx != null) {
                            val tc = OrderingTemporaryBiDirectional(prevx, c, Direction.RIGHT, cnr!!)
                            val from = gridGroups[prevx]!!
                            val to = gridGroups[c]!!
                            from.sortLink(Direction.RIGHT, to, LINK_WEIGHT, true, Int.MAX_VALUE, single(tc))
                            to.sortLink(Direction.LEFT, from, LINK_WEIGHT, true, Int.MAX_VALUE, single(tc))
                        }
                        if (c !== prevy && prevy != null) {
                            val tc = OrderingTemporaryBiDirectional(prevy, c, Direction.DOWN, cnr!!)
                            val from = gridGroups[prevy]!!
                            val to = gridGroups[c]!!
                            from.sortLink(Direction.DOWN, to, LINK_WEIGHT, true, Int.MAX_VALUE, single(tc))
                            to.sortLink(Direction.UP, from, LINK_WEIGHT, true, Int.MAX_VALUE, single(tc))
                        }
                    }
                }
            } else {
                var prev: Connected? = null
                for (c in (ord as Container).getContents()) {
                    if (c is Connected) {
                        createLeafGroup(c, prev, pMap)
                        prev = c
                    }
                }
            }
        }
        return g
    }

    private fun setupLinks(o: DiagramElement) {
        if (o is Connected) {
            for (c in o.getLinks()) {
                if (!allLinks.contains(c)) {
                    allLinks.add(c)
                    ch.checkForContainerContradiction(c)
                    if (isConnectionRendered(c)) {
                        val to = getConnectionEnd(c.otherEnd(o))
                        val from = getConnectionEnd(o)
                        var d = c.getDrawDirectionFrom(o)
                        if (isConnectionContradicting(c)) {
                            d = null
                        }
                        val ordering = false ///c instanceof OrderingTemporaryBiDirectional;
                        from.sortLink(d, to, LINK_WEIGHT, ordering, getLinkRank(c), single(c))
                        to.sortLink(reverse(d), from, LINK_WEIGHT, ordering, getLinkRank(c), single(c))
                    }
                }
            }
        }
        if (o is Container) {
            for (o2 in o.getContents()) {
                setupLinks(o2)
            }
        }
    }

    private fun getLinkRank(c: Connection): Int {
//		if (c instanceof OrderingTemporaryBiDirectional) {
//			return Integer.MAX_VALUE;
//		} else
        return if (c.getDrawDirection() != null) {
            c.getRank()
        } else {
            0
        }
    }

    private fun needsLeafGroup(ord: Connected): Boolean {
        return if (ord is Diagram && !hasConnectedContents(ord as Diagram)) {
            // we need at least one group in the GroupPhase, so if the diagram is empty, return a
            // single leaf group.
            true
        } else !em.requiresPlanarizationCornerVertices(ord)
    }

    private fun hasConnectedContents(d: Diagram): Boolean {
        for (de in d.getContents()) {
            if (de is Connected) {
                return true
            }
        }
        return false
    }

    private fun addContainerOrderingInfo(
        current: Connected,
        prev: Connected?,
        cnr: Container?,
        gridDimension: Dimension?
    ) {
        if (prev == null) return
        val l = cnr!!.getLayout()
        var d: Direction? = null
        if (TEMPORARY_NEEDED.contains(l)) {
            d = getDirectionForLayout(l)
        } else if (gridDimension != null) {
            d = if (gridDimension === Dimension.H) Direction.RIGHT else Direction.DOWN
        }
        if (d != null) {
            val tc = OrderingTemporaryBiDirectional(prev, current, d, cnr)
            val from = getConnectionEnd(prev)
            val to = getConnectionEnd(current)
            from.sortLink(d, to, LINK_WEIGHT, true, Int.MAX_VALUE, single(tc))
            to.sortLink(reverse(d), from, LINK_WEIGHT, true, Int.MAX_VALUE, single(tc))
        }
    }

    @JvmField
    var groupCount = 0
    @JvmField
    var containerCount = 0

    abstract inner class Group protected constructor(var type: GroupAxis, val linkManager: LinkManager) : Deterministic {
        /**
         * Returns the leaf group number (or numbers for compound group)
         * composing this group.
         */
        val leafList : String by lazy {
            val gs = mutableSetOf<Int>()
            addLeafGroupNumbersToSet(gs)
            val sortedList = gs.sorted();
            sortedList.toString()
        }


        /**
         * TODO: use the group number for hashcode in a more normal way.
         */
        override fun getID(): String {
            return "g$groupNumber"
        }

        abstract fun addLeafGroupNumbersToSet(s: MutableSet<Int>)

        var layout: Layout? = null
        var groupOrdinal = 0
            protected set
        protected var hashCode = 0

        /**
         * Live means that the group is ready to merge at the moment.
         */
        var isLive = false
        override fun hashCode(): Int {
            return hashCode
        }

        val isActive: Boolean
            get() = type == null || type!!.active
        var size = 0

        fun processAllLeavingLinks(compound: Boolean, mask: Int, lp: LinkProcessor) {
            linkManager.processAllLeavingLinks(compound, mask, lp)
        }

        fun getLink(g: Group): LinkDetail? {
            return linkManager[g]
        }

        fun getAxis() : GroupAxis {
            return type
        }

        abstract fun processLowestLevelLinks(lp: LinkProcessor)

        /**
         * Returns true if this group is lg, or it contains it somehow in the hierarchy
         */
        abstract operator fun contains(lg: Group): Boolean

        /**
         * Returns the number of nested levels below this group
         */
        abstract val groupHeight: Int

        var groupNumber = groupCount++ + 1

        fun log(log: Kite9Log) {
            log.send("Group: $this")
            log.send("  Links:", linkManager.forLogging())
        }

        abstract val hints: Map<String, Float?>

        init {
            type.setGroup(this)
            linkManager.setGroup(this)
        }
    }

    /**
     * Represents the relative positions of two other groups within the diagram, allowing the immediate contents
     * of any container to be expressed as a binary tree.
     */
    inner class CompoundGroup constructor(val a: Group, val b: Group, axis: GroupAxis, lm: LinkManager, treatAsLeaf: Boolean) :
        Group(axis, lm) {

        override val groupHeight: Int

        var internalLinkA: LinkDetail? = null
            private set
        var internalLinkB: LinkDetail? = null
            private set

        override val hints: Map<String, Float?>

        private val treatAsLeaf: Boolean

        override fun addLeafGroupNumbersToSet(s: MutableSet<Int>) {
            if (treatAsLeaf) {
                s.add(groupNumber)
            } else {
                a.addLeafGroupNumbersToSet(s)
                b.addLeafGroupNumbersToSet(s)
            }
        }

        /**
         * This will process [OrderingTemporaryConnection]s first, so that if there is
         * a contradiction in the links, it will occur on one of the Link - Connections.
         */
        private fun fileLinks(linksGroup: Group, toGroup: Group) {
            linksGroup.processAllLeavingLinks(true, linkManager.allMask(), object : LinkProcessor {
                override fun process(notUsed: Group, g: Group, ld: LinkDetail) {
                    fileLink(linksGroup, g, toGroup, ld)
                }
            })
        }

        private fun fileLink(from: Group, to: Group, merging: Group, ld: LinkDetail) {
            val internal = merging.contains(to)
            if (!internal) {
                linkManager.sortLink(ld!!)
            } else {
                if (merging === to) {
                    if (from === a) {
                        log.send("Setting internal A:$ld $to")
                        internalLinkA = ld
                    } else {
                        log.send("Setting internal B:$ld $to")
                        internalLinkB = ld
                    }
                }
            }
        }

        override fun toString(): String {
            return "[$groupNumber$a,$b:$type]"
        }

        override fun contains(lg: Group): Boolean {
            if (this === lg) {
                return true
            }
            /**
             * Obviously, we can't contain bigger groups than ourselves.
             */
            if (lg.size >= size) {
                return false
            }
            /**
             * Also, can't create later-created groups than this.
             */
            return if (lg.groupNumber > groupNumber) {
                false
            } else a.contains(lg) || b.contains(lg)
        }

        override fun processLowestLevelLinks(lp: LinkProcessor) {
            a.processLowestLevelLinks(lp)
            b.processLowestLevelLinks(lp)
        }

        init {
            size = a.size + b.size
            groupHeight = Math.max(a.groupHeight, b.groupHeight) + 1
            groupOrdinal = Math.min(a.groupOrdinal, b.groupOrdinal)
            this.treatAsLeaf = treatAsLeaf
            if (!treatAsLeaf) {
                // this is done so that a different compound group containing the same leaves can
                // occupy the same position in a hashmap
                hashCode = a.hashCode() + b.hashCode()
            } else {
                hashCode = hashCodeGenerator.nextInt()
            }
            fileLinks(a, b)
            fileLinks(b, a) // internals kept from one side to avoid duplication
            hints = merge(a.hints, b.hints)
        }
    }

    /**
     * Represents a single vertex (glyph, context) within the diagram
     */
    inner class LeafGroup(var contained: Connected?, var container: Container?, axis: GroupAxis, lm: LinkManager) :
        Group(axis, lm) {
        override fun toString(): String {
            return "[" + groupNumber + contained + "(" + (if (container is Diagram) "" else " c: " + container) + "," + type + ")]"
        }

        override fun contains(lg: Group): Boolean {
            return this === lg
        }

        override fun processLowestLevelLinks(lp: LinkProcessor) {
            processAllLeavingLinks(false, linkManager.allMask(), lp)
        }

        override val groupHeight: Int
            get() = 0

        fun sortLink(
            d: Direction?,
            otherGroup: Group,
            linkValue: Float,
            ordering: Boolean,
            linkRank: Int,
            c: Iterable<BiDirectional<Connected>>
        ) {
            linkManager.sortLink(d, otherGroup, linkValue, ordering, linkRank, c)
        }

        override val hints: Map<String, Float?>
            get() = emptyMap()

        override fun addLeafGroupNumbersToSet(s: MutableSet<Int>) {
            s.add(groupNumber)
        }

        init {

            // layout is the container layout at this level
            layout = if (container != null) container!!.getLayout() else null
            if (contained is Container) {
                containerCount++
            }
            groupOrdinal = groupNumber
            size = 1
            hashCode = hashCodeGenerator.nextInt()
        }
    }

    private fun single(c: BiDirectional<Connected>): Set<BiDirectional<Connected>> {
        return setOf(c)
    }

    /**
     * This has to handle decomposition
     */
    private fun getConnectionEnd(oe: Connected): LeafGroup {
        val otherGroup = pMap[oe]
        return if (otherGroup == null) {
            val decomp = LeafGroup(null, oe as Container, ab.createAxis(), ab.createLinkManager())
            allGroups.add(decomp)
            decomp
        } else {
            otherGroup
        }
    }

    companion object {
        const val LINK_WEIGHT = 1f
        var log = Kite9Log(object : Logable {
            override val isLoggingEnabled: Boolean
                get() = true
            override val prefix: String
                get() = "GP  "
        })

        @JvmStatic
        fun isHorizontalDirection(drawDirection: Direction?): Boolean {
            return drawDirection === Direction.LEFT || drawDirection === Direction.RIGHT
        }

        @JvmStatic
        fun isVerticalDirection(drawDirection: Direction?): Boolean {
            return drawDirection === Direction.UP || drawDirection === Direction.DOWN
        }

        private val TEMPORARY_NEEDED: Set<Layout> = setOf(Layout.LEFT, Layout.RIGHT, Layout.UP, Layout.DOWN)
        @JvmStatic
        fun getLayoutForDirection(currentDirection: Direction?): Layout? {
            return if (currentDirection == null) null else when (currentDirection) {
                Direction.RIGHT -> Layout.RIGHT
                Direction.LEFT -> Layout.LEFT
                Direction.DOWN -> Layout.DOWN
                Direction.UP -> Layout.UP
                else -> null
            }
        }

        @JvmStatic
        fun getDirectionForLayout(currentDirection: Layout?): Direction {
            return when (currentDirection) {
                Layout.RIGHT -> Direction.RIGHT
                Layout.LEFT -> Direction.LEFT
                Layout.DOWN -> Direction.DOWN
                Layout.UP -> Direction.UP
                Layout.VERTICAL, Layout.HORIZONTAL -> throw LogicException(
                    "Wasn't expecting direction: $currentDirection"
                )
                else -> throw LogicException("Wasn't expecting direction: $currentDirection")
            }
        }
    }

    init {
        pMap = LinkedHashMap(elements * 2)
        allGroups = LinkedHashSet(elements * 2)
        this.ab = ab
        this.ch = ch
        this.gp = gp
        this.em = em
        hashCodeGenerator = Random(elements.toLong())
        createLeafGroup(top as Connected, null, pMap)
        setupLinks(top)
        for (group in allGroups) {
            group.log(log)
        }
    }
}