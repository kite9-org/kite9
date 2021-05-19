package org.kite9.diagram.visualization.planarization.rhd

import org.kite9.diagram.common.BiDirectional
import org.kite9.diagram.common.elements.RoutingInfo
import org.kite9.diagram.common.elements.grid.GridPositioner
import org.kite9.diagram.common.elements.mapping.CornerVertices
import org.kite9.diagram.common.elements.mapping.ElementMapper
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.common.objects.Bounds
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.*
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.model.position.Layout.Companion.reverse
import org.kite9.diagram.visualization.planarization.Planarization
import org.kite9.diagram.visualization.planarization.PlanarizationBuilder
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.AbstractCompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.AbstractLeafGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.group.DirectedGroupAxis
import org.kite9.diagram.visualization.planarization.rhd.grouping.generators.GeneratorBasedGroupingStrategyImpl
import org.kite9.diagram.visualization.planarization.rhd.layout.DirectionLayoutStrategy
import org.kite9.diagram.visualization.planarization.rhd.layout.LayoutStrategy
import org.kite9.diagram.visualization.planarization.rhd.layout.MostNetworkedFirstLayoutQueue
import org.kite9.diagram.visualization.planarization.rhd.links.BasicContradictionHandler
import org.kite9.diagram.visualization.planarization.rhd.links.ConnectionManager
import org.kite9.diagram.visualization.planarization.rhd.links.ContradictionHandler
import org.kite9.diagram.visualization.planarization.rhd.links.RankBasedConnectionQueue
import org.kite9.diagram.visualization.planarization.rhd.position.PositionRoutableHandler2D
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D.DPos
import org.kite9.diagram.visualization.planarization.rhd.position.VertexPositioner
import org.kite9.diagram.visualization.planarization.rhd.position.VertexPositionerImpl

/**
 * Rob's Hierarchical Decomposition Planarization Builder is a 4-phase process:
 *
 *  * grouping
 *  * layout
 *  * positioning
 *  * vertex ordering
 *
 *
 * The grouping phase divides the orderables at each level into hierarchical groups
 * based on how many connections they have with other groups.
 *
 * The layout phase takes a group and works out whether the two merge groups
 * within it should be placed horizontally or vertically within their container.
 * This means there are 4^c options for this merge where c is the number of
 * containers with parts of both groups.
 *
 * Positioning is the process of assigning x/y positions to the leaf groups and containers.
 *
 * Finally, the results of this are used to create the list of vertices to
 * return, with their positions set by the layout strategy.  We also add vertices for any container corners at this stage.
 *
 * @author moffatr
 */
abstract class RHDPlanarizationBuilder(protected var em: ElementMapper, protected var gridHelp: GridPositioner) : PlanarizationBuilder, Logable {

    private val log: Kite9Log = Kite9Log.instance(this)
	protected var routableReader: RoutableHandler2D = PositionRoutableHandler2D()
    private var vp: VertexPositioner = newVertexPositioner()

    protected abstract fun buildPlanarization(
        c: Diagram,
        vertexOrder: List<Vertex>,
        initialUninsertedConnections: Collection<BiDirectional<Connected>>,
        sortedContainerContents: Map<Container, List<Connected>>
    ): Planarization

    internal enum class PlanarizationRun {
        FIRST, REDO, DONE
    }

    fun countConnectedElements(de: DiagramElement): Int {
        var out: Int = 0
        if (de is ConnectedRectangular) {
            out++
            if (de is Container) {
                for (c: DiagramElement in (de as Container).getContents()) {
                    out += countConnectedElements(c)
                }
            }
        }
        return out
    }

    override fun planarize(c: Diagram): Planarization {
        val elements: Int = countConnectedElements(c)
        var run: PlanarizationRun = PlanarizationRun.FIRST
        val out: MutableList<Vertex> = ArrayList(elements * 2)
        var connections: ConnectionManager? = null
        var sortedContainerContents: MutableMap<Container, MutableList<ConnectedRectangular>> = mutableMapOf()
        try {
            while (run != PlanarizationRun.DONE) {
                routableReader = PositionRoutableHandler2D()
                vp = newVertexPositioner()
                val ch: ContradictionHandler = BasicContradictionHandler(em)
                val strategy = GeneratorBasedGroupingStrategyImpl(c, elements, ch, gridHelp, em)
                strategy.buildInitialGroups()
                val mr = strategy.group()
                if (!log.go()) {
                    log.send("Created Groups:", mr.groups())
                }
                if (mr.groups().size > 1) {
                    throw LogicException("Should end up with a single group")
                }
                val topGroup: Group = mr.groups().iterator().next()
                if (!log.go()) {
                    outputGroupInfo(topGroup, 0)
                }

                // Layout
                val layout: LayoutStrategy = DirectionLayoutStrategy(routableReader)
                layout.layout(mr, MostNetworkedFirstLayoutQueue(topGroup.groupNumber))

                // positioning
                connections = RankBasedConnectionQueue(routableReader)
                buildPositionMap(topGroup, connections)
                if (!log.go()) {
                    outputGroupInfo(topGroup, 0)
                }
                if (connections.hasContradictions()) {
                    if (!checkLayoutIsConsistent(c)) {
                        if (run == PlanarizationRun.FIRST) {
                            log.send("Contradiction forces regroup")
                            run = PlanarizationRun.REDO
                            continue
                        }
                    }
                }

                // vertex ordering
                sortedContainerContents = HashMap(topGroup.groupNumber * 2)
                instantiateContainerVertices(c)
                buildVertexList(null, c, null, out, sortedContainerContents)
                sortContents(out, routableReader.getTopLevelBounds(true), routableReader.getTopLevelBounds(false))
                run = PlanarizationRun.DONE
            }
        } finally {
            LAST_PLANARIZATION_DEBUG = out
        }
        val planOut: Planarization = buildPlanarization(c, out, connections!!, sortedContainerContents)
        (planOut as RHDPlanarizationImpl).setRoutableReader((routableReader))
        return planOut
    }

    private fun newVertexPositioner() = VertexPositionerImpl(em, routableReader) { a, b -> compareDiagramElements(a, b) }

    /**
     * This makes sure all the container vertices have the correct anchors before we position them.
     */
    private fun instantiateContainerVertices(c: DiagramElement) {
        if (em.requiresPlanarizationCornerVertices(c)) {
            em.getOuterCornerVertices(c)
            if (c is Container) {
                for (de: DiagramElement in c.getContents()) {
                    if (de is Port) {
                        em.getPlanarizationVertex(de)
                    } else {
                        instantiateContainerVertices(de)
                    }
                }
            }
        }
    }

    /**
     * Potentially expensive, but checks to make sure that none of the positions overlap.
     */
    private fun checkLayoutIsConsistent(c: Container): Boolean {
        val contents: List<DiagramElement> = c.getContents()
        for (i in contents.indices) {
            val ci: DiagramElement = contents.get(i)
            if (ci is ConnectedRectangular) {
                for (j in 0 until i) {
                    val cj: DiagramElement = contents.get(j)
                    if (cj is ConnectedRectangular) {
                        if (overlaps(ci, cj)) {
                            log.error("Overlap in positions of: $ci  $cj")
                            return false
                        }
                    }
                }
                if (em.requiresPlanarizationCornerVertices(ci)) {
                    if (!checkLayoutIsConsistent(ci as Container)) {
                        return false
                    }
                }
            }
        }
        return true
    }

    private fun overlaps(a: DiagramElement, b: DiagramElement): Boolean {
        val ria: RoutingInfo? = routableReader.getPlacedPosition(a)
        val rib: RoutingInfo? = routableReader.getPlacedPosition(b)
        return (routableReader.overlaps(ria!!, rib!!))
    }

    fun outputGroupInfo(g: Group, spc: Int) {
        val sb: StringBuilder = StringBuilder(spc)
        for (i in 0 until spc) {
            sb.append(" ")
        }
        val axis = g.axis as DirectedGroupAxis
        val l: Layout? = g.layout
        log.send(
            (sb.toString() + g.groupNumber +
                    " " + (if ((g is AbstractLeafGroup)) g.toString() else axis)
                    + "   " + routableReader.getPlacedPosition(g) + "  " + l + " " + (if (g.axis.isLayoutRequired) "LR " else " ")
                    + (if ((g is AbstractCompoundGroup)) (g.a.groupNumber).toString() + " " + (g.b.groupNumber) else ""))
        )
        if (g is AbstractCompoundGroup) {
//			if ((l==Layout.UP) || (l==Layout.LEFT)) {
//				outputGroupInfo(((CompoundGroup) g).getB(), spc+1);
//				outputGroupInfo(((CompoundGroup) g).getA(), spc+1);
//			} else {
            outputGroupInfo(g.a, spc + 1)
            outputGroupInfo(g.b, spc + 1)
            //			}
        }
    }

    fun explain(g: Group, spc: Int) {
        val sb: StringBuilder = StringBuilder(spc)
        for (i in 0 until spc) {
            sb.append(" ")
        }
        val axis = g.axis as DirectedGroupAxis
        log.send(sb.toString() + g.groupNumber + "   " + routableReader.getPlacedPosition(g))
        val hParent: Group? = axis.horizParentGroup
        val vParent: Group? = axis.vertParentGroup
        if (vParent !== hParent) {
            if (hParent != null) {
                log.send(
                    (sb.toString()
                            + "horiz: "
                            + (if ((g === (hParent as AbstractCompoundGroup).b)) hParent.layout else reverse(
                        hParent
                            .layout
                    )))
                )
                explain(hParent, spc + 1)
            }
            if (vParent != null) {
                log.send(
                    (sb.toString()
                            + "vert: "
                            + (if ((g === (vParent as AbstractCompoundGroup).b)) vParent.layout else reverse(
                        vParent
                            .layout
                    )))
                )
                explain(vParent, spc + 1)
            }
        } else {
            // vParent == hParent
            if (vParent != null) {
                log.send(
                    (sb.toString()
                            + "pos: "
                            + (if ((g === (vParent as AbstractCompoundGroup).b)) vParent.layout else reverse(
                        vParent
                            .layout
                    )))
                )
                explain(vParent, spc + 1)
            }
        }
    }

    private fun buildPositionMap(start: Group, connections: ConnectionManager) {
        if (start is AbstractCompoundGroup) {
            val cg: AbstractCompoundGroup = start
            log.send("Processing Group: " + start.groupNumber)
            buildPositionMap(cg.a, connections)
            buildPositionMap(cg.b, connections)
            connections.handleLinks(cg)
        } else {
            val lg: AbstractLeafGroup = start as AbstractLeafGroup

            log.send("Processing Group: $lg")
            // g is a leaf group.  can we place it?
            val l: Connected? = lg.connected
            val c: Container? = lg.container

            // sizing
            if (l !is Port) {
                val ri: RoutingInfo = lg.axis.getPosition((routableReader), false)
                vp.checkMinimumGridSizes(ri)
                if (lg.occupiesSpace()) {
                    routableReader.setPlacedPosition(l as Rectangular, ri)
                }
                ensureContainerBoundsAreLargeEnough(ri, c, lg)
            }

            // leaf groups shouldn't have connections, so these won't get rendered
            connections.handleLinks(lg)
        }
    }

    private fun ensureContainerBoundsAreLargeEnough(ri: RoutingInfo, c: Container?, lg: AbstractLeafGroup) {
        var c: Container? = c
        var l: ConnectedRectangular
        while (c != null) {
            // make sure container bounds are big enough for the contents
            var cri: RoutingInfo? = routableReader.getPlacedPosition(c)
            if (cri == null) {
                cri = routableReader.emptyBounds()
            }
            val cri2: RoutingInfo = routableReader.increaseBounds(cri, ri)
            if (!(cri2 == cri)) {
                log.send("Increased bounds of $c to $cri2 due to $lg")
            }
            routableReader.setPlacedPosition(c, cri2)
            l = c as ConnectedRectangular
            c = l.getContainer()
        }
    }

    /**
     * Constructs the list of vertices in no particular order.
     */
    private fun buildVertexList(
        before: Connected?,
        c: DiagramElement,
        after: Connected?,
        out: MutableList<Vertex>,
        sortedContainerContents: MutableMap<Container, MutableList<ConnectedRectangular>>
    ) {
        if (em.hasOuterCornerVertices(c)) {
            val cvs: CornerVertices = em.getOuterCornerVertices(c)
            val bounds: RoutingInfo? = routableReader.getPlacedPosition(c)
            log.send("Placed position of container: $c is $bounds")
            vp.setPerimeterVertexPositions(before, c, after, cvs, out)
            if (c is Container) {
                buildVertexListForContainerContents(out, c, sortedContainerContents)
            }
        } else {
            vp.setCentralVertexPosition(c, out)
        }
        return
    }

    private fun buildVertexListForContainerContents(
        out: MutableList<Vertex>,
        container: Container,
        sortedContainerContents: MutableMap<Container, MutableList<ConnectedRectangular>>
    ) {
        val layingOut: Boolean = container.getLayout() != null
        val contents: MutableList<ConnectedRectangular> = getConnectedRectangularContainerContents(container.getContents())
        if (layingOut) {
            contents.sortWith { arg0: Connected, arg1: Connected ->
                compareDiagramElements(arg0, arg1)
            }
            sortedContainerContents[container] = contents
        }
        if (contents.size == 0) {
            return
        }
        var conBefore: Connected?
        var current: Connected? = null
        var conAfter: Connected? = null
        var start: Boolean = true
        val iterator: Iterator<Connected?> = contents.iterator()
        while (start || (current != null)) {
            conBefore = current
            current = conAfter
            conAfter = getNextConnected(iterator)
            if (current != null) {
                buildVertexList(conBefore, current, conAfter, out, sortedContainerContents)
                start = false
            }
        }
    }

    private fun getNextConnected(iterator: Iterator<Connected?>): Connected? {
        if (iterator.hasNext()) {
            return iterator.next()
        }
        return null
    }

    protected fun getConnectedRectangularContainerContents(contents: List<DiagramElement>): MutableList<ConnectedRectangular> {
        val out = contents
            .filterIsInstance<ConnectedRectangular>()
            .toMutableList()
        return out
    }

    /**
     * This implements a kind of quad sort, where we look at each vertex and ascribe it to TL, TR, BL, BR
     * quadrants.  We do that over and over until there's only a single vertex in each quadrant.
     */
    private fun sortContents(`in`: MutableList<Vertex>, x: Bounds, y: Bounds) {
        if (`in`.size > 1) {
            log.send("Sorting: $`in`")
            val left: Bounds = routableReader.narrow(Layout.LEFT, x, true, false)
            val right: Bounds = routableReader.narrow(Layout.RIGHT, x, true, false)
            val top: Bounds = routableReader.narrow(Layout.UP, y, false, false)
            val bottom: Bounds = routableReader.narrow(Layout.DOWN, y, false, false)
            val topRight: MutableList<Vertex> = ArrayList(`in`.size / 2)
            val topLeft: MutableList<Vertex> = ArrayList(`in`.size / 2)
            val bottomRight: MutableList<Vertex> = ArrayList(`in`.size / 2)
            val bottomLeft: MutableList<Vertex> = ArrayList(`in`.size / 2)
            var mergeTopAndBottom: Boolean = false
            var mergeLeftAndRight: Boolean = false
            for (vertex: Vertex in `in`) {
                val vri: RoutingInfo? = vertex.routingInfo
                val vx: Bounds = routableReader.getBoundsOf(vri, true)
                val vy: Bounds = routableReader.getBoundsOf(vri, false)
                val inLeft: Boolean = routableReader.compareBounds(vx, left) == DPos.OVERLAP
                val inRight: Boolean = routableReader.compareBounds(vx, right) == DPos.OVERLAP
                val inTop: Boolean = routableReader.compareBounds(vy, top) == DPos.OVERLAP
                val inBottom: Boolean = routableReader.compareBounds(vy, bottom) == DPos.OVERLAP
                if (inTop && inBottom) {
                    mergeTopAndBottom = true
                }
                if (inLeft && inRight) {
                    mergeLeftAndRight = true
                }
                if ((!inTop && !inBottom) || (!inLeft && !inRight)) {
                    throw LogicException("Vertex not within either bounds$vri $vertex")
                }
                if (inLeft) {
                    if (inTop) {
                        topLeft.add(vertex)
                    } else {
                        bottomLeft.add(vertex)
                    }
                } else {
                    if (inTop) {
                        topRight.add(vertex)
                    } else {
                        bottomRight.add(vertex)
                    }
                }
            }
            if (mergeLeftAndRight && mergeTopAndBottom) {
                // we should have a single vertex, so you need to sort the old fashioned way
                sortContentsOldStyle(`in`)
            } else if (mergeLeftAndRight) {
                `in`.clear()
                topRight.addAll(topLeft)
                bottomRight.addAll(bottomLeft)
                sortContents(topRight, x, top)
                sortContents(bottomRight, x, bottom)
                `in`.addAll(topRight)
                `in`.addAll(bottomRight)
            } else if (mergeTopAndBottom) {
                `in`.clear()
                topRight.addAll(bottomRight)
                topLeft.addAll(bottomLeft)
                sortContents(topRight, right, y)
                sortContents(topLeft, left, y)
                `in`.addAll(topLeft)
                `in`.addAll(topRight)
            } else {
                `in`.clear()
                sortContents(topLeft, left, top)
                sortContents(topRight, right, top)
                sortContents(bottomLeft, left, bottom)
                sortContents(bottomRight, right, bottom)
                `in`.addAll(topLeft)
                `in`.addAll(topRight)
                `in`.addAll(bottomLeft)
                `in`.addAll(bottomRight)
            }
        }
    }

    /**
     * Simple Y,X sort, where Y has priority.
     *
     * This is insufficient on it's own, because we end up with situations where in order to "get around" one large vertex we
     * move out-of-position with respect to another vertex.  Because of the difference in vertex sizes, you basically
     * can't just sort on middle position and hope things are in the right order.
     */
    private fun sortContentsOldStyle(c: MutableList<Vertex>) {
        c.sortWith {arg0: Vertex, arg1: Vertex ->
            val ri0: RoutingInfo? = arg0.routingInfo
            val ri1: RoutingInfo? = arg1.routingInfo
            var out: Int = 0
            val yc: DPos = routableReader.compare(ri0, ri1, false)
            if (yc == DPos.BEFORE) {
                out = -1
            } else if (yc == DPos.AFTER) {
                out = 1
            }
            if (out == 0) {
                val xc: DPos = routableReader.compare(ri0, ri1, true)
                if (xc == DPos.BEFORE) {
                    out = -1
                } else if (xc == DPos.AFTER) {
                    out = 1
                }
            }
            if (out == 0) {
                log.error("Contents overlap: $arg0 $arg1")
                throw LogicException("Contents overlap: $arg0 $arg1")
            }

            //System.out.println("Comparing: " + arg0 + " " + arg1 + " " + out);
            out
        }
    }

    override val prefix: String
        get() = "GRPW"
    override val isLoggingEnabled: Boolean
        get() = true

    /**
     * Returns the sort order for the two elements according to their groupwise position
     * within a container.
     */
    protected fun compareDiagramElements(a: DiagramElement, b: DiagramElement): Int {
        var parent: DiagramElement? = a.getParent()
        if (b.getParent() !== parent) {
            parent = getCommonContainer(a, b)
        }
        val l: Layout? = (parent as Container?)!!.getLayout()
        if (l == null) {
            return 0
        } else {
            var dp: DPos? = null
            when (l) {
                Layout.UP, Layout.DOWN, Layout.VERTICAL -> dp = routableReader.compare(a, b, false)
                Layout.LEFT, Layout.RIGHT, Layout.HORIZONTAL -> dp = routableReader.compare(a, b, true)
                Layout.GRID -> {
                    dp = routableReader.compare(a, b, false)
                    dp = if ((dp == DPos.OVERLAP)) routableReader.compare(a, b, true) else dp
                }
            }
            if (dp == DPos.BEFORE) {
                return -1
            } else if (dp == DPos.AFTER) {
                return 1
            } else {
                return 0
                //	throw new LogicException("Elements within a container shouldn't overlap");
            }
        }
    }

    protected fun getCommonContainer(from: DiagramElement?, to: DiagramElement?): Container? {
        var from: DiagramElement? = from
        var to: DiagramElement? = to
        while (from !== to) {
            val depthFrom: Int = from!!.getDepth()
            val depthTo: Int = to!!.getDepth()
            if (depthFrom < depthTo) {
                to = to.getParent()
            } else if (depthFrom > depthTo) {
                from = from.getParent()
            } else {
                to = to.getParent()
                from = from.getParent()
            }
        }
        return from as Container?
    }

    companion object {

		var LAST_PLANARIZATION_DEBUG: List<Vertex>? = null
        val CHANGE_CONTAINER_ORDER: Boolean = true
    }

    init {
        LAST_PLANARIZATION_DEBUG = null
    }
}