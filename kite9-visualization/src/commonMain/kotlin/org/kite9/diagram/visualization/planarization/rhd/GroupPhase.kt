package org.kite9.diagram.visualization.planarization.rhd

import org.kite9.diagram.common.BiDirectional
import org.kite9.diagram.common.algorithms.det.UnorderedSet
import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.common.elements.factory.DiagramElementFactory
import org.kite9.diagram.common.elements.grid.GridPositioner
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.*
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Direction.Companion.reverse
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.model.style.BorderTraversal
import org.kite9.diagram.model.style.Measurement
import org.kite9.diagram.visualization.planarization.Tools.Companion.isConnectionContradicting
import org.kite9.diagram.visualization.planarization.Tools.Companion.isConnectionRendered
import org.kite9.diagram.visualization.planarization.rhd.grouping.TemporaryContainerHub
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.LeafGroup
import org.kite9.diagram.visualization.planarization.rhd.links.ContradictionHandler
import org.kite9.diagram.visualization.planarization.rhd.links.OrderingTemporaryBiDirectional
import kotlin.random.Random


/**
 * A GroupPhase is responsible for creating the Group data structures out of the
 * vertices, and holding various related lookup maps.
 *
 * @author robmoffat
 */
abstract class GroupPhase(
    val top: DiagramElement,
    val elements: Int,
    val ch: ContradictionHandler,
    val gp: GridPositioner,
    var ef: DiagramElementFactory<*>
) : GroupBuilder, Logable {

    val log = Kite9Log.instance(this)

    override val prefix: String
        get() = "GS  "

    override val isLoggingEnabled: Boolean
        get() = true


    val allGroups: MutableSet<LeafGroup> = LinkedHashSet(elements * 2)
    private val pMap: MutableMap<Connected, LeafGroup> = LinkedHashMap(elements * 2)
    private val allLinks: MutableSet<Connection> = UnorderedSet(1000)
    protected val hashCodeGenerator = Random(elements.toLong())

    /**
     * Creates leaf groups and any ordering between them, recursively.
     * @param ord  The object to create the group for
     * @param prev1 Previous element in the container
     * @param pMap Map of Connected to LeafGroups (created)
     */
    private fun populateLeafGroups(
        ord: Connected,
        prev1: ConnectedRectangular?,
        pMap: MutableMap<Connected, LeafGroup>
    ): LeafGroup? {
        if (pMap[ord] != null) {
            throw LogicException("Diagram Element $ord appears multiple times in the diagram definition")
        }
        val cnr = ord.getContainer()
        val leaf = needsLeafGroup(ord)
        var g: LeafGroup? = null
        if (leaf) {
            g = createLeafGroup(ord, cnr)
            pMap[ord] = g
            allGroups.add(g)
        }
        if (prev1 != null && prev1 !== ord && ord is ConnectedRectangular) {
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
                            var gg = populateLeafGroups(de as ConnectedRectangular, null, pMap)
                            if (gg == null) {
                                gg = getConnectionEnd(de, Pair(x, y))
                            }
                            gridGroups[de] = gg
                        }
                    }
                }

                // link them up
                for (y in grid.indices) {
                    for (x in grid[0].indices) {
                        val prevy = (if (y > 0) grid[y - 1][x] else null) as ConnectedRectangular?
                        val prevx = (if (x > 0) grid[y][x - 1] else null) as ConnectedRectangular?
                        val c = grid[y][x] as ConnectedRectangular
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
                var prev: ConnectedRectangular? = null
                var hasPorts = false
                var hasRectangulars = false
                for (c in (ord as Container).getContents()) {
                    if (c is Connected) {
                        populateLeafGroups(c, prev, pMap)
                        if (c is ConnectedRectangular) {
                            prev = c
                            hasRectangulars = true
                        } else if (c is Port) {
                            hasPorts = true
                        }
                    }
                }

                if (hasPorts) {
                    // ensure we respect the port ordering, if we can ascertain one.

                    val allPorts = (ord as Container).getContents()
                        .filterIsInstance<Port>()
                        .groupBy { it.getPortDirection() }

                    allPorts.forEach { (d, ports) ->
                        val portGroups = ports.groupBy {
                            val pp = it.getContainerPosition(Direction.getDimension(d))
                            when (pp.type) {
                                Measurement.PIXELS -> if (pp.amount < 0) 0 else 1
                                Measurement.PERCENTAGE, Measurement.NONE -> 2
                            }
                        }

                        portGroups.values.forEach { l ->
                            val sortedPorts = l.sortedBy {
                                val side = it.getPortDirection()
                                it.getContainerPosition(Direction.getDimension(side)).amount
                            }
                            val directionBasedOnSide = when(d) {
                                Direction.UP, Direction.DOWN -> Direction.RIGHT
                                Direction.LEFT, Direction.RIGHT -> Direction.DOWN
                            }
                            var prev : Port? = null
                            sortedPorts.forEach { next ->
                                if (prev != null) {
                                    val from = pMap[prev]
                                    val to = pMap[next]
                                    if ((from != null) && (to != null)) {
                                        val tc = OrderingTemporaryBiDirectional(prev, next, directionBasedOnSide, ord)
                                        from.sortLink(directionBasedOnSide, to, LINK_WEIGHT, true, Int.MAX_VALUE, single(tc))
                                        to.sortLink(Direction.reverse(directionBasedOnSide), from, LINK_WEIGHT, true, Int.MAX_VALUE, single(tc))
                                    }
                                }
                                prev = next
                            }
                        }
                    }
                }

                if (hasPorts && !hasRectangulars) {
                    val tc = ef.createTemporaryConnected(ord, "-port-placeholder")
                    populateLeafGroups(tc, null, pMap)
                }
            }
        }
        return g
    }

    private fun setupLinks(o: DiagramElement) {
        if (o is Connected) {
            if (o is Port) {
                ch.checkForPortContradiction(o)
            }
            for (c in o.getLinks()) {
                if (!allLinks.contains(c)) {
                    allLinks.add(c)
                    ch.checkForContainerContradiction(c)
                    if (isConnectionRendered(c)) {
                        val to = getConnectionEnd(c.otherEnd(o), null)
                        val from = getConnectionEnd(o, null)
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
        } else {
            val out = !hasLowerLevelContents(ord)
            out
        }
    }

    fun hasLowerLevelContents(c: DiagramElement): Boolean {
        if (c is Diagram) {
            return true
        }
        // does anything inside it have connections?
        if (c is Container) {
            for (de in c.getContents()) {
                if (hasNestedConnections(de)) {
                    return true
                }
            }

            // are connections allowed to pass through it?
            val canTraverse = isElementTraversible(c)
            if (canTraverse && hasNestedConnections(c)) {
                return true
            }
        }

        // is it embedded in a grid?  If yes, use corners
        if (c is ConnectedRectangular) {
            val l = if (c.getParent() == null) null else (c.getParent() as Container?)!!.getLayout()
            return l == Layout.GRID
        }

        return false
    }

    private fun isElementTraversible(c: DiagramElement): Boolean {
        return isElementTraversible(c, Direction.UP) ||
                isElementTraversible(c, Direction.DOWN) ||
                isElementTraversible(c, Direction.LEFT) ||
                isElementTraversible(c, Direction.RIGHT)
    }

    private fun isElementTraversible(c: DiagramElement, d: Direction): Boolean {
        return if (c is Container) {
            c.getTraversalRule(d) == BorderTraversal.ALWAYS
        } else false
    }

    private var hasConnections: MutableMap<DiagramElement?, Boolean> = HashMap()


    fun hasNestedConnections(c: DiagramElement?): Boolean {
        if (hasConnections.containsKey(c)) {
            return hasConnections[c]!!
        }
        var has = false
        if (c is Connected) {
            has = c.getLinks().size > 0
        }
        if (has == false && c is Container) {
            for (de in c.getContents()) {
                if (hasNestedConnections(de)) {
                    has = true
                    break
                }
            }
        }
        hasConnections[c] = has
        return has
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
        current: ConnectedRectangular,
        prev: ConnectedRectangular?,
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
            val from = getConnectionEnd(prev, null)
            val to = getConnectionEnd(current, null)
            from.sortLink(d, to, LINK_WEIGHT, true, Int.MAX_VALUE, single(tc))
            to.sortLink(reverse(d), from, LINK_WEIGHT, true, Int.MAX_VALUE, single(tc))
        }
    }


    var groupCount = 0

    var containerCount = 0

    private fun single(c: BiDirectional<Connected>): Set<BiDirectional<Connected>> {
        return setOf(c)
    }

    /**
     * This has to handle decomposition
     */
    private fun getConnectionEnd(oe: Connected, gridPos: Pair<Int, Int>?): LeafGroup {
        val otherGroup = pMap[oe]
        return if (otherGroup == null) {
            if (needsLeafGroup(oe)) {
                val decomp = createLeafGroup(oe, oe.getContainer())
                allGroups.add(decomp)
                decomp
            } else {
                val hub = TemporaryContainerHub(oe as Container)
                val decomp = createLeafGroup(hub, oe as Container)
                oe.addTemporaryContent(hub)
                if (gridPos != null) {
                    hub.gridPosition = gridPos
                }
                allGroups.add(decomp)
                decomp
            }
        } else {
            otherGroup
        }
    }

    companion object {
        const val LINK_WEIGHT = 1f
        var log = Kite9Log.instance(object : Logable {
            override val isLoggingEnabled: Boolean
                get() = true
            override val prefix: String
                get() = "GP  "
        })


        fun isHorizontalDirection(drawDirection: Direction?): Boolean {
            return drawDirection === Direction.LEFT || drawDirection === Direction.RIGHT
        }


        fun isVerticalDirection(drawDirection: Direction?): Boolean {
            return drawDirection === Direction.UP || drawDirection === Direction.DOWN
        }

        private val TEMPORARY_NEEDED: Set<Layout> = setOf(Layout.LEFT, Layout.RIGHT, Layout.UP, Layout.DOWN)

        fun getLayoutForDirection(currentDirection: Direction?): Layout? {
            return if (currentDirection == null) null else when (currentDirection) {
                Direction.RIGHT -> Layout.RIGHT
                Direction.LEFT -> Layout.LEFT
                Direction.DOWN -> Layout.DOWN
                Direction.UP -> Layout.UP
            }
        }


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



    override fun buildInitialGroups() {
        populateLeafGroups(top as ConnectedRectangular, null, pMap)
        setupLinks(top)
        for (group in allGroups) {
            group.log(log)
        }
    }
}