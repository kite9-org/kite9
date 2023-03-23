package org.kite9.diagram.visualization.planarization.mgt.router

import org.kite9.diagram.common.algorithms.ssp.State
import org.kite9.diagram.common.elements.edge.Edge
import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.planarization.mgt.BorderEdge
import org.kite9.diagram.visualization.planarization.mgt.MGTPlanarization

class ContainerEdgeRouteFinder(p: MGTPlanarization, rh: RoutableReader, e: BorderEdge) : AbstractRouteFinder(
    p, rh, e.getTo().routingInfo!!, getExpensiveAxis(e), getBoundedAxis(e), e
) {
    var to: Vertex = e.getTo()

    override fun createInitialPaths(pq: State<LocatedEdgePath>) {
        val from = e.getFrom()
        createInitialPathsFrom(pq, from)
    }

    private fun createInitialPathsFrom(pq: State<LocatedEdgePath>, from: Vertex) {
        // remove backwards?
        when (e.getDrawDirection()!!) {
            Direction.LEFT, Direction.UP -> {
                generatePaths(
                    null,
                    p.getAboveBackwardLinks(from),
                    pq,
                    from,
                    Going.BACKWARDS,
                    PlanarizationSide.ENDING_ABOVE
                )
                generatePaths(
                    null,
                    p.getBelowBackwardLinks(from),
                    pq,
                    from,
                    Going.BACKWARDS,
                    PlanarizationSide.ENDING_BELOW
                )
            }
            Direction.DOWN, Direction.RIGHT -> {
                generatePaths(
                    null,
                    p.getAboveForwardLinks(from),
                    pq,
                    from,
                    Going.FORWARDS,
                    PlanarizationSide.ENDING_ABOVE
                )
                generatePaths(
                    null,
                    p.getBelowForwardLinks(from),
                    pq,
                    from,
                    Going.FORWARDS,
                    PlanarizationSide.ENDING_BELOW
                )
            }
        }
    }

    /**
     * Rules for passing next to indvidual, unconnected vertices when doing container edges.
     */
    override fun canTravel(pathVertex: Int, endingDirection: Going?, pathAbove: Boolean): Boolean {
        return when (e.getDrawDirection()) {
            Direction.LEFT, Direction.UP -> endingDirection === Going.BACKWARDS
            Direction.DOWN, Direction.RIGHT -> endingDirection === Going.FORWARDS
            else -> throw LogicException("Was expecting a direction for the container border edge")
        }
    }

    override fun canAddToQueue(ep: LocatedEdgePath?): Boolean {
        return true
    }

    override fun canCross(edge: Edge?, forward: EdgePath?, above: Boolean): Boolean {
        return false
    }

    override fun canRouteToVertex(
        from: Vertex, outsideOf: PlanarizationEdge?,
        above: Boolean, g: Going?, arriving: Boolean
    ): Boolean {
        return true
    }

    override fun isTerminationVertex(v: Int): Boolean {
        return p.vertexOrder[v] === to
    }

    companion object {
        private fun getBoundedAxis(e: Edge): Axis? {
            val edgeDir = e.getDrawDirection()
            return when (edgeDir) {
                Direction.UP, Direction.DOWN -> Axis.VERTICAL
                Direction.LEFT, Direction.RIGHT -> Axis.HORIZONTAL
                else -> null
            }
        }

        private fun getExpensiveAxis(e: Edge): Axis? {
            val edgeDir = e.getDrawDirection()
            return when (edgeDir) {
                Direction.UP, Direction.DOWN -> Axis.HORIZONTAL
                Direction.LEFT, Direction.RIGHT -> Axis.VERTICAL
                else -> null
            }
        }
    }

    init {
        pathDirection = e.getDrawDirection()
        entryDirection = pathDirection
        exitDirection = pathDirection
    }
}