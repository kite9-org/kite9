package org.kite9.diagram.visualization.planarization.mgt.router

import org.kite9.diagram.common.BiDirectional
import org.kite9.diagram.common.algorithms.ssp.NoFurtherPathException
import org.kite9.diagram.common.algorithms.ssp.State
import org.kite9.diagram.common.elements.RoutingInfo
import org.kite9.diagram.common.elements.edge.BiDirectionalPlanarizationEdge
import org.kite9.diagram.common.elements.edge.Edge
import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.common.elements.mapping.ConnectionEdge
import org.kite9.diagram.common.elements.mapping.ElementMapper
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex.Companion.isMax
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex.Companion.isMin
import org.kite9.diagram.common.elements.vertex.MultiElementVertex
import org.kite9.diagram.common.elements.vertex.NoElementVertex
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.ConnectedRectangular
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Direction.Companion.reverse
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.visualization.planarization.Tools.Companion.isUnderlyingContradicting
import org.kite9.diagram.visualization.planarization.mgt.BorderEdge
import org.kite9.diagram.visualization.planarization.mgt.MGTPlanarization
import org.kite9.diagram.visualization.planarization.ordering.EdgeOrdering
import org.kite9.diagram.visualization.planarization.ordering.VertexEdgeOrdering

abstract class AbstractBiDiEdgeRouteFinder(
    p: MGTPlanarization,
    rh: RoutableReader,
    ci: BiDirectionalPlanarizationEdge,
    em: ElementMapper,
    path: Direction?,
    it: CrossingType,
    gt: GeographyType
) : AbstractRouteFinder(
    p, rh, getEndZone(rh, ci), getExpensiveAxis(ci, gt), getBoundedAxis(ci, gt), ci
) {

	var maximumBoundedAxisDistance: Double?
    var allowedCrossingDirections: Set<Direction?>

	var startZone: RoutingInfo?
    var completeZone: RoutingInfo

	var em: ElementMapper
    var it: CrossingType
    var gt: GeographyType

    /**
     * Figure out if the edge is crossing in a perpendicular direction.
     */
    protected fun canCrossBidiEdge(a: BiDirectionalPlanarizationEdge, goingDown: Boolean): Boolean {
        if (a.getDrawDirection() == null) {
            return true
        }

        // check that the crossing will be orthogonal
        val crossDirection = Direction.values()[(a.getDrawDirection()!!.ordinal + (if (goingDown) -1 else 1) + 4) % 4]
        val ok = allowedCrossingDirections.contains(crossDirection)
        return if (ok) {
            if (pathDirection != null) {
                // check that the two edges intersect
                val c = a.getOriginalUnderlying() as BiDirectional<ConnectedRectangular>
                val from = routeHandler.getPlacedPosition(c.getFrom())
                val to = routeHandler.getPlacedPosition(c.getTo())
                val area = routeHandler.increaseBounds(from!!, to!!)
                return if (routeHandler.overlaps(area, completeZone)) {
                    true
                } else {
                    log.send("$a is not local to $e - no cross")
                    false
                }
            }
            true
        } else {
            false
        }
    }

    protected open fun canCrossBorderEdge(crossing: BorderEdge?, ep: EdgePath?): Boolean {
        return false
    }

    override fun canRouteToVertex(
        to: Vertex,
        edge: PlanarizationEdge?,
        pathAbove: Boolean,
        g: Going?,
        arriving: Boolean
    ): Boolean {
        var edge = edge
        if (to is NoElementVertex) {
            return false
        }
        val forwards = g === Going.FORWARDS
        var clockwise = forwards == pathAbove
        val dd: Direction?
        if (!arriving) {
            clockwise = !clockwise
            dd = exitDirection
        } else {
            dd = reverse(entryDirection)
        }
        if (dd == null) {
            return true
        }
        val eo = p.edgeOrderings[to] as VertexEdgeOrdering? ?: return true
        if (eo.getEdgeDirections() !== EdgeOrdering.MUTLIPLE_DIRECTIONS) {
            return true
        }

        //		System.out.println(p);
        //		System.out.println("Can route to="+to+" edge = "+edge+" above="+pathAbove+" going="+g+" arriving="+arriving+ " direction= "+dd);
        return if (edge == null) {
            val forwardSet = if (arriving) g === Going.BACKWARDS else g === Going.FORWARDS
            edge = p.getFirstEdgeAfterPlanarizationLine(to, forwardSet, pathAbove)
            //			System.out.println(out);
            eo.canInsert(edge, dd, !clockwise, log)
        } else {
            //			System.out.println(out);
            eo.canInsert(edge, dd, clockwise, log)
        }
    }

    /**
     * When the path crosses an edge, the direction of the edge must be 90 degrees advanced from the path direction,
     * otherwise there will be a contradiction in the planarization.
     */
    override fun canCross(e2: Edge?, ep: EdgePath?, goingDown: Boolean): Boolean {
        return if (e2 is BorderEdge) {
            canCrossBorderEdge(e2 as BorderEdge?, ep)
        } else if (e2 is BiDirectionalPlanarizationEdge) {
            // regular connection edge
            canCrossBidiEdge(e2, goingDown)
        } else {
            throw LogicException("Don't know edge type: " + e2)
        }
    }

    private fun decidePreferredSide(ci: Edge): PlanarizationSide? {
        return if (ci is ConnectionEdge) {
            val id = ci.getID()
            val hash = id.hashCode()
            if (hash % 2 == 1) PlanarizationSide.ENDING_ABOVE else PlanarizationSide.ENDING_BELOW
        } else {
            null
        }
    }

    private fun getCompleteZone(ci: BiDirectionalPlanarizationEdge): RoutingInfo {
        val from = getRoutingZone(routeHandler, ci, true)
        val to = getRoutingZone(routeHandler, ci, false)
        return routeHandler.increaseBounds(from!!, to!!)
    }

    private fun getDirection(edgeDir: Direction?, it: CrossingType): Direction? {
        return if (it === CrossingType.UNDIRECTED) {
            null
        } else {
            edgeDir
        }
    }

    /**
     * Provides an extra directional check
     */
    override fun createShortestPath(): LocatedEdgePath {
        if (e.getDrawDirection() != null && gt !== GeographyType.RELAXED) {
            // check that destination is in the correct direction for this to even work
            if (!isBasicEdgeDirectionAllowed) {
                throw NoFurtherPathException("Edge direction incompatible with start/end positions")
            }
        }
        return super.createShortestPath()
    }

    private val isBasicEdgeDirectionAllowed: Boolean
        private get() = when (e.getDrawDirection()) {
            Direction.UP -> startZone!!.compareY(endZone) == 1
            Direction.DOWN -> startZone!!.compareY(endZone) == -1
            Direction.LEFT -> startZone!!.compareX(endZone) == 1
            Direction.RIGHT -> startZone!!.compareX(endZone) == -1
            else -> true
        }

    private fun getIllegalEdgeCrossAxis(edgeDir: Direction?, it: CrossingType): Axis? {
        return if (it === CrossingType.UNDIRECTED || edgeDir == null) {
            null
        } else {
            when (edgeDir) {
                Direction.UP, Direction.DOWN -> Axis.VERTICAL
                Direction.LEFT, Direction.RIGHT -> Axis.HORIZONTAL
            }
        }
        return null
    }

    override fun canAddToQueue(ep: LocatedEdgePath?): Boolean {
        return if (maximumBoundedAxisDistance != null) {
            val currentAxisTotal = ep!!.costing.minimumBoundedAxisDistance
            val out = currentAxisTotal <= maximumBoundedAxisDistance!! + tolerance
            if (!out) {
                log.send("Exceeded maximumBoundedAxisDistance: $currentAxisTotal (max=$maximumBoundedAxisDistance) for: $ep")
            }
            out
        } else {
            true
        }
    }

    private fun getCrossingDirections(edgeDir: Direction?, it: CrossingType): Set<Direction?> {
        if (edgeDir == null || it === CrossingType.UNDIRECTED) {
            return setOf(Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT)
        }
        return if (it === CrossingType.NOT_BACKWARDS) {
            val out= mutableSetOf(Direction.LEFT, Direction.RIGHT, Direction.DOWN, Direction.UP)
            out.remove(reverse(edgeDir))
            out
        } else {
            setOf(edgeDir)
        }
    }

    protected open fun allowConnectionsToContainerContents(): Boolean {
        return true
    }

    protected open fun createInitialPathsFrom(pq: State<LocatedEdgePath>, from: Vertex?) {
        try {
            generatePaths(
                null,
                p.getAboveBackwardLinks(from!!),
                pq,
                from,
                Going.BACKWARDS,
                PlanarizationSide.ENDING_ABOVE
            )
            generatePaths(null, p.getAboveForwardLinks(from), pq, from, Going.FORWARDS, PlanarizationSide.ENDING_ABOVE)
            generatePaths(
                null,
                p.getBelowBackwardLinks(from),
                pq,
                from,
                Going.BACKWARDS,
                PlanarizationSide.ENDING_BELOW
            )
            generatePaths(null, p.getBelowForwardLinks(from), pq, from, Going.FORWARDS, PlanarizationSide.ENDING_BELOW)
        } catch (e: RuntimeException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }

    private fun getMaximumBoundedAxisDistance(ax: Axis?): Double? {
        val bpe = e as BiDirectionalPlanarizationEdge
        if (bpe.getDrawDirection() == null) {
            return null
        }

        // work out furthest possible vertices apart
        val from = getFarthestVertex(bpe.fromUnderlying, bpe.getDrawDirection())
        val to = getFarthestVertex(bpe.toUnderlying, reverse(bpe.getDrawDirection()))
        return if (ax != null) {
            var minPath = routeHandler.move(null, from.routingInfo!!, null)
            minPath = routeHandler.move(minPath, to.routingInfo!!, null)
            if (ax === Axis.HORIZONTAL) {
                minPath.getHorizontalRunningCost()
            } else if (ax === Axis.VERTICAL) {
                minPath.getVerticalRunningCost()
            } else {
                throw LogicException("No axis to process")
            }
        } else {
            null
        }
    }

    private fun getFarthestVertex(c: DiagramElement?, d: Direction?): Vertex {
        return if (em.hasOuterCornerVertices(c!!)) {
            val cv = em.getOuterCornerVertices(c)
            when (d) {
                Direction.UP, Direction.LEFT -> cv.getBottomRight()
                Direction.DOWN, Direction.RIGHT -> cv.getTopLeft()
                else -> cv.getTopLeft()
            }
        } else {
            em.getPlanarizationVertex(c)
        }
    }

    override fun canTravel(pathVertex: Int, endingDirection: Going?, b: Boolean): Boolean {
        return true
    }

    /**
     * Ensures that we are starting/terminating on a vertex on the right side of the
     * container we are leaving/arriving at.
     */
    protected fun onCorrectSideOfContainer(v: MultiCornerVertex, termination: Boolean): Boolean {
        val dd = (if (termination) exitDirection else entryDirection) ?: return true
        return if (it !== CrossingType.STRICT) {
            true
        } else when (dd) {
            Direction.UP -> if (termination) isMax(v.yOrdinal) else isMin(
                v.yOrdinal
            )
            Direction.DOWN -> if (termination) isMin(v.yOrdinal) else isMax(
                v.yOrdinal
            )
            Direction.LEFT -> if (termination) isMax(v.xOrdinal) else isMin(
                v.xOrdinal
            )
            Direction.RIGHT -> if (termination) isMin(v.xOrdinal) else isMax(
                v.xOrdinal
            )
        }
        throw LogicException("Can't determine whether we can arrive/leave at this vertex")
    }

    private val preferredSide: PlanarizationSide?
    override fun compareFavouredSide(a: PlanarizationSide?, b: PlanarizationSide?): Int {
        return if (a === preferredSide) {
            -1
        } else {
            1
        }
    }

    companion object {
        private fun getEndZone(rh: RoutableReader, ci: BiDirectionalPlanarizationEdge): RoutingInfo {
            return getRoutingZone(rh, ci, false)!!
        }


		fun checkContainerNotWithinGrid(c: Container) {
            val parent = c.getContainer()
            if (parent != null && parent.getLayout() === Layout.GRID) {
                throw EdgeRoutingException("Edge can't be routed as it can't come from a container embedded in a grid: $c")
            }
        }

        /**
         * The routing zone is the area of the DiagramElement, as opposed to the vertices representing it.
         */
        private fun getRoutingZone(
            rh: RoutableReader,
            ci: BiDirectionalPlanarizationEdge,
            from: Boolean
        ): RoutingInfo? {
            val v = if (from) ci.getFrom() else ci.getTo()
            if (v is MultiElementVertex) {
                // we don't care about the vertex specifically, it could be one of a number...
                    // so return the whole area containing all the vertices
                return rh.getPlacedPosition((if (from) ci.fromUnderlying else ci.toUnderlying)!!)
            } else {
                return v.routingInfo
            }
        }

        private fun getBoundedAxis(e: Edge, gt: GeographyType): Axis? {
            val dir = e.getDrawDirection() ?: return null
            if (gt === GeographyType.RELAXED) {
                return null
            }
            var out: Axis? = null
            out = when (dir) {
                Direction.UP, Direction.DOWN -> Axis.VERTICAL
                Direction.LEFT, Direction.RIGHT -> Axis.HORIZONTAL
            }
            return out
        }

        private fun getExpensiveAxis(e: Edge, it: GeographyType): Axis? {
            val edgeDir = e.getDrawDirection()
            val flip = isUnderlyingContradicting(e)
            if (edgeDir == null) {
                return null
            }
            var out: Axis? = null
            out = when (edgeDir) {
                Direction.UP, Direction.DOWN -> if (flip) Axis.VERTICAL else Axis.HORIZONTAL
                Direction.LEFT, Direction.RIGHT -> if (flip) Axis.HORIZONTAL else Axis.VERTICAL
            }
            return out
        }
    }

    init {
        startZone = getRoutingZone(rh, ci, true)
        val endZone = getRoutingZone(rh, ci, false)
        completeZone = getCompleteZone(ci)
        this.em = em
        this.it = it
        this.gt = gt
        maximumBoundedAxisDistance = getMaximumBoundedAxisDistance(bounded)
        allowedCrossingDirections = getCrossingDirections(path, it)
        illegalEdgeCross = getIllegalEdgeCrossAxis(path, it)
        pathDirection = getDirection(path, it)
        if (pathDirection != null) {
            entryDirection = pathDirection
            exitDirection = pathDirection
        } else if (ci.getDrawDirection() == null) {
            entryDirection = ci.getFromArrivalSide()
            exitDirection = ci.getToArrivalSide()
        }
        if (rh.isWithin(startZone!!, endZone!!) || rh.isWithin(endZone, startZone!!)) {
            throw EdgeRoutingException("Edge can't be routed as it is from something inside something else: $e")
        }
        preferredSide = decidePreferredSide(ci)
        log.send("Preferred Side: $preferredSide")
        log.send("Route Finding for: $e")
    }
}