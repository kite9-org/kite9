package org.kite9.diagram.visualization.planarization.rhd

import org.kite9.diagram.common.BiDirectional
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
import org.kite9.diagram.visualization.planarization.rhd.grouping.ConnectedGroupLinkNode
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.LeafGroup
import org.kite9.diagram.visualization.planarization.rhd.links.ContradictionHandler
import org.kite9.diagram.visualization.planarization.rhd.links.OrderingTemporaryBiDirectional
import kotlin.collections.contains
import kotlin.math.max
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
    private val linkEndMap: MutableMap<Pair<Connection, Boolean>, LeafGroup> = LinkedHashMap(elements * 2)
    private val done = mutableSetOf<DiagramElement>()
    protected val hashCodeGenerator = Random(elements.toLong())

    private fun populateContainerPortsLeafGroups(ord: Rectangular) : Int {
        var created = 0
        // creates a LeafGroup for every port in the container,
        // ensure we respect the port ordering, if we can ascertain one.
        val allPorts = ord.getContents()
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
                var prevLg : LeafGroup? = null
                sortedPorts.forEach { next ->
                    val lg = createLeafGroup(next, ord)
                    created ++
                    if (prevLg != null) {
                        val tc = OrderingTemporaryBiDirectional(prevLg.connected, next, directionBasedOnSide)
                        prevLg.sortLink(directionBasedOnSide, lg, LINK_WEIGHT, true, Int.MAX_VALUE, single(tc))
                        lg.sortLink(Direction.reverse(directionBasedOnSide), prevLg, LINK_WEIGHT, true, Int.MAX_VALUE, single(tc))
                    }
                    prevLg = lg
                }
            }
        }

        return created
    }

    /**
     * Creates LeafGroups between elements where the layout is left, right, up, down
     * to enforce that ordering.
     */
    private fun populateNonGridLayoutLeafGroups(ord: Rectangular) : Int {
        var created = 0
        val l = ord.getLayout()

        fun addContainerOrderingInfo(
            current: LeafGroup,
            prev: LeafGroup,
        ) {
            val d = getDirectionForLayout(l)
            val tc = OrderingTemporaryBiDirectional(prev.connected, current.connected, d)
            prev.sortLink(d, current, LINK_WEIGHT, true, Int.MAX_VALUE, single(tc))
            current.sortLink(reverse(d), prev, LINK_WEIGHT, true, Int.MAX_VALUE, single(tc))
        }

        val contents = ord.getContents()
            .filterIsInstance<ConnectedRectangular>()

        var prevLg = null

        contents.forEach { nextE ->
            created += populateLeafGroups(nextE)
            if ((prevLg != null) && (TEMPORARY_NEEDED.contains(l))) {
                val gln = ConnectedGroupLinkNode(nextE, "-hub")
                nextE.addTemporaryContent(gln)
                val nextLg = createLeafGroup(gln, nextE)
                created++
                addContainerOrderingInfo(nextLg, prevLg)
            }
        }

        return created
    }

    private fun populateGridContentsLeafGroups(ord: Rectangular) : Int {
        var created = 0
        // need to iterate in 2d
        val grid = gp.placeOnGrid(ord)
        val gridGroups = mutableMapOf<Pair<Int, Int>, LeafGroup>()

        // create unconnected groups
        for (y in grid.indices) {
            for (x in grid[0].indices) {
                val pos = Pair(x, y)
                val de = grid[y][x]
                created += populateLeafGroups(de as ConnectedRectangular)
                val gln = ConnectedGroupLinkNode(ord, "-grid", pos)
                ord.addTemporaryContent(gln)
                val ggGrid = createLeafGroup(gln, de)
                created++
                gridGroups[pos] = ggGrid
            }
        }

        // link them up
        for (y in grid.indices) {
            for (x in grid[0].indices) {
                val pos = Pair(x, y)
                val above = if (y>0) Pair(x,y -1 ) else null
                val before = if (x>0) Pair(x-1, y) else null
                val c = grid[y][x]
                val g = gridGroups[pos]!!
                val aboveG = gridGroups[above]
                val beforeG = gridGroups[before]
                if (g !== beforeG && beforeG != null) {
                    val tc = OrderingTemporaryBiDirectional(beforeG.connected, g.connected, Direction.RIGHT)
                    beforeG.sortLink(Direction.RIGHT, g, LINK_WEIGHT, true, Int.MAX_VALUE, single(tc))
                    g.sortLink(Direction.LEFT, beforeG, LINK_WEIGHT, true, Int.MAX_VALUE, single(tc))
                }
                if (g !== aboveG && aboveG != null) {
                    val tc = OrderingTemporaryBiDirectional(aboveG.connected, g.connected, Direction.DOWN)
                    aboveG.sortLink(Direction.DOWN, g, LINK_WEIGHT, true, Int.MAX_VALUE, single(tc))
                    g.sortLink(Direction.UP, aboveG, LINK_WEIGHT, true, Int.MAX_VALUE, single(tc))
                }
            }
        }

        return created
    }

    fun populateEmptyElementLeafGroup(ord: Rectangular) : Int {
        // ok, so nothing connects to this, but we still need to create a
        // leaf group for it of some sort.
        val gln = ConnectedGroupLinkNode(ord, "-bareleaf")
        ord.addTemporaryContent(gln)
        createLeafGroup(gln, ord)
        return 1
    }

    fun populateConnectedElementLeafGroups(ord: Rectangular) : Int {
        var created = 0
        val connectionsByDimension =
            (ord as? Connected)?.getLinks()
                ?.groupBy {
                    if (it.getDrawDirection() == null)
                        null
                    else
                        Direction.getDimension(it.getDrawDirection()!!)
                } ?: emptyMap()

        val directedLeafGroupsNeeded = max(
            connectionsByDimension[Dimension.H]?.size ?: 0,
            connectionsByDimension[Dimension.V]?.size ?: 0)

        // map the directed links
        for (i in 1..directedLeafGroupsNeeded) {
            val gln = ConnectedGroupLinkNode(ord, "-dl$i")
            ord.addTemporaryContent(gln)
            val lg = createLeafGroup(gln, ord)
            created ++
            val hLink = connectionsByDimension[Dimension.H]?.getOrNull(i-1)
            val vLink = connectionsByDimension[Dimension.V]?.getOrNull(i-1)

            if (hLink != null) {
                val hFrom = hLink?.getFrom() == ord
                linkEndMap[Pair(hLink, hFrom)] = lg
            }

            if (vLink != null) {
                val vFrom = vLink?.getFrom() == ord
                linkEndMap[Pair(vLink, vFrom)] = lg
            }
        }

        // for now, map all undirected links to the same node
        val undirectedConnections = connectionsByDimension[null]
        if (undirectedConnections?.isNotEmpty() ?: false) {
            val gln = ConnectedGroupLinkNode(ord, "-und")
            ord.addTemporaryContent(gln)
            val lg = createLeafGroup(gln, ord)
            created ++
            undirectedConnections.forEach {
                val from = it.getFrom() == ord
                linkEndMap[Pair(it, from)] = lg
            }
        }

        return created
    }

    /**
     * Creates leaf groups and any ordering between them, recursively.
     * @param ord  The object to create the group for
     */
    private fun populateLeafGroups(
        ord: Rectangular
    ) : Int {
        if (done.contains(ord)) {
            throw LogicException("Diagram Element $ord appears multiple times in the diagram definition")
        }

        val leaf = needsSelfLeafGroups(ord)
        var created = 0

        if (!leaf) {
            // handle element contents of this element
            val l = ord.getLayout()
            if (l === Layout.GRID) {
                created += populateGridContentsLeafGroups(ord)
            } else {
                created += populateNonGridLayoutLeafGroups(ord)
            }

            // handle port contents of this element
            created += populateContainerPortsLeafGroups(ord)

            // handle connections to this element
            created += populateConnectedElementLeafGroups(ord)
            if (created == 0) {
                created += populateEmptyElementLeafGroup(ord)
            }
        } else {
            created +=populateConnectedElementLeafGroups(ord)
            if (created == 0) {
                created += populateEmptyElementLeafGroup(ord)
            }
        }

        return created
    }

    private fun setupLinks() {

        fun getLinkRank(c: Connection): Int {
            return if (c.getDrawDirection() != null) {
                c.getRank()
            } else {
                0
            }
        }

        linkEndMap.entries
            .groupBy { it.key.first }
            .forEach { (c, value) ->
                if (value.size != 2) {
                    throw LogicException("Should be two ends for every connection")
                }
                val first = value[0]
                val second = value[1]
                val from = if (first.key.second) first.value else second.value
                val to = if (first.key.second) second.value else first.value
                from.sortLink(c.getDrawDirectionFrom(from.container as Connected), to, LINK_WEIGHT, true, getLinkRank(c), single(c))
                to.sortLink(c.getDrawDirectionFrom(to.container as Connected), from, LINK_WEIGHT, true, Int.MAX_VALUE, single(c))
            }
    }


    private fun needsSelfLeafGroups(ord: Rectangular): Boolean {
        return if (ord is Diagram && !hasConnectedContents(ord)) {
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
        if (c is Rectangular) {
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
            val l = c.getContainer()?.getLayout()
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
        return if (c is Rectangular) {
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
        if (has == false && c is Rectangular) {
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




    var groupCount = 0

    var containerCount = 0

    private fun single(c: BiDirectional<Connected>): Set<BiDirectional<Connected>> {
        return setOf(c)
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
        populateLeafGroups(top as ConnectedRectangular)
        setupLinks()
        for (group in allGroups) {
            group.log(log)
        }
    }
}