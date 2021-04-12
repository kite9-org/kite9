package org.kite9.diagram.visualization.planarization.mgt.router

import org.kite9.diagram.common.algorithms.ssp.State
import org.kite9.diagram.common.elements.edge.BiDirectionalPlanarizationEdge
import org.kite9.diagram.common.elements.edge.Edge
import org.kite9.diagram.common.elements.mapping.ConnectionEdge
import org.kite9.diagram.common.elements.mapping.ElementMapper
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex
import org.kite9.diagram.common.elements.vertex.PortVertex
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.ConnectedRectangular
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Direction.Companion.isHorizontal
import org.kite9.diagram.model.position.Direction.Companion.reverse
import org.kite9.diagram.model.style.BorderTraversal
import org.kite9.diagram.visualization.planarization.Tools.Companion.isUnderlyingContradicting
import org.kite9.diagram.visualization.planarization.mgt.BorderEdge
import org.kite9.diagram.visualization.planarization.mgt.MGTPlanarization

class ConnectionEdgeRouteFinder(
    p: MGTPlanarization,
    rh: RoutableReader,
    ci: ConnectionEdge,
    em: ElementMapper,
    path: Direction?,
    it: CrossingType,
    gt: GeographyType
) : AbstractBiDiEdgeRouteFinder(
    p, rh, ci, em, path, it, gt
) {

    var mustCrossContainers: Set<Container>

    override fun canCrossBorderEdge(crossing: BorderEdge?, ep: EdgePath?): Boolean {
        val traversalRule = getTraversalRule(crossing)
        if (traversalRule === BorderTraversal.PREVENT) {
            return false
        } else if (traversalRule === BorderTraversal.LEAVING) {
            val allPresent = mustCrossContainers.containsAll(crossing!!.getDiagramElements().keys)
            if (!allPresent) {
                return false
            }
        }
        return if (pathDirection == null) {
            true
        } else {
            if (isHorizontal(pathDirection!!) == isHorizontal(
                    crossing!!.getDrawDirection()
                )
            ) {
                // edges must be perpendicular
                return false
            }
            var entering = crossing.getElementForSide(pathDirection!!)
            if (entering == null) {
                // we are actually leaving a container
                entering = crossing.getElementForSide(reverse(pathDirection)!!)!!
                    .getParent()
            }

            // check that the container is positioned somewhere that intersects with the edge direction 
            if (!checkContainerPathIntersection(ep, entering, pathDirection!!)) {
                log.send("$e can't cross into $entering")
                return false
            }

//			if (incidentDirection != expectedDirection) {
//				log.send(e+" can't cross over "+crossing+" in direction: "+incidentDirection+", expected: "+expectedDirection);
//				return false;
//			}
            true
        }
    }

    private fun getTraversalRule(crossing: BorderEdge?): BorderTraversal? {
        return crossing!!.borderTraversal
    }

    private fun checkContainerPathIntersection(ep: EdgePath?, c: DiagramElement?, ed: Direction): Boolean {
        val cri = routeHandler.getPlacedPosition(c!!)
        return routeHandler.isInPlane(
            cri!!,
            startZone!!,
            pathDirection === Direction.RIGHT || pathDirection === Direction.LEFT
        )
    }

    private fun getMustCrossContainers(from: Connected, to: Connected): Set<Container> {
        var from: Connected? = from
        var to: Connected? = to
        val out: MutableSet<Container> = HashSet()
        while (from !== to) {
            val fromDepth = from!!.getDepth()
            val toDepth = to!!.getDepth()
            if (fromDepth > toDepth) {
                if (from is Container) {
                    out.add(from as Container)
                }
                from = from.getParent() as Connected?
            } else if (toDepth > fromDepth) {
                if (to is Container) {
                    out.add(to as Container)
                }
                to = to.getParent() as Connected?
            } else {
                if (from is Container) {
                    out.add(from as Container)
                }
                if (to is Container) {
                    out.add(to as Container)
                }
                from = from.getParent() as Connected?
                to = to.getParent() as Connected?
            }
        }
        return out
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

    override fun allowConnectionsToContainerContents(): Boolean {
        return true
    }

    override fun createInitialPaths(pq: State<LocatedEdgePath>) {
        val from = e.getFrom()
        if (from is PortVertex) {
            createInitialPathsFrom(pq, from)
        } else if (from is MultiCornerVertex) {
            val c = (e as BiDirectionalPlanarizationEdge).getFromConnected() as Container?
            checkContainerNotWithinGrid(c!!)
            val cvs = em.getOuterCornerVertices(c)
            for (v in cvs.getPerimeterVertices()) {
                if (!v.isPartOf(c)) {
                    // ensure anchors are set correctly for the perimeter.
                    v.addAnchor(null, null, c)
                }
                if (onCorrectSideOfContainer(v, false)) {
                    createInitialPathsFrom(pq, v)
                }
            }
            if (allowConnectionsToContainerContents()) {
                for (con in c.getContents()) {
                    if (con is Connected) {
                        if (con !is Container) {
                            val vcon = em.getPlanarizationVertex(con)
                            createInitialPathsFrom(pq, vcon)
                        }
                    }
                }
            }
        } else {
            createInitialPathsFrom(pq, from)
        }
    }

    override fun createInitialPathsFrom(pq: State<LocatedEdgePath>, from: Vertex?) {
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
        if (e.getFrom() is MultiCornerVertex || e.getTo() is MultiCornerVertex) {
            // this method is unsafe for containers because they have gutters around the positions of the things inside them.
            return null
        }
        return if (ax != null) {
            var minPath = routeHandler.move(null, startZone!!, null)
            minPath = routeHandler.move(minPath, endZone, null)
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

    override fun canTravel(pathVertex: Int, endingDirection: Going?, b: Boolean): Boolean {
        return true
    }

    override fun isTerminationVertex(v: Int): Boolean {
        val originalUnderlying: DiagramElement? = (e as ConnectionEdge).getToConnected()
        val candidate = p.vertexOrder[v]
        return if (candidate is MultiCornerVertex) {

            //DiagramElement und = candidate.getOriginalUnderlying();
            // return true if this is a container vertex for the container we're trying to get to
            if (candidate.isPartOf(originalUnderlying) && onCorrectSideOfContainer(
                    candidate,
                    true
                )
            ) {
                return true
            }
            var out = false
            if (allowConnectionsToContainerContents()) {
                // return false if this element is not in the correct container.
                if (!candidateIsContainedIn(candidate, originalUnderlying)) {
                    return false
                }
                val ri = candidate.routingInfo ?: return false

                // return true if the vertex is within the container.
                out = routeHandler.isWithin(endZone, ri)
            }
            out
        } else {
            candidate === e.getTo()
        }
    }

    private fun candidateIsContainedIn(candidate: MultiCornerVertex, originalUnderlying: DiagramElement?): Boolean {
        for (de in candidate.getDiagramElements()) {
            if (de.getParent() === originalUnderlying) {
                return true
            }
        }
        return false
    }

    companion object {
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
        mustCrossContainers =
            getMustCrossContainers(ci.getOriginalUnderlying().getFrom(), ci.getOriginalUnderlying().getTo())
    }
}