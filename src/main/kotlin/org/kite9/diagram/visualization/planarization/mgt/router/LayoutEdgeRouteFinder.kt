package org.kite9.diagram.visualization.planarization.mgt.router

import org.kite9.diagram.common.algorithms.ssp.State
import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.common.elements.mapping.ContainerLayoutEdge
import org.kite9.diagram.common.elements.mapping.ElementMapper
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex.Companion.getOrdForXDirection
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex.Companion.getOrdForYDirection
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Direction.Companion.reverse
import org.kite9.diagram.visualization.planarization.mgt.MGTPlanarization

/**
 * For layout edges, we usually know which vertex the connection must go from and to.
 * So limit the search to just those.
 *
 * @author robmoffat
 */
class LayoutEdgeRouteFinder(
    p: MGTPlanarization,
    rh: RoutableReader,
    ci: ContainerLayoutEdge,
    em: ElementMapper,
    edgeDir: Direction
) : AbstractBiDiEdgeRouteFinder(
    p, rh, ci, em, edgeDir, CrossingType.STRICT, GeographyType.STRICT
) {
    var start: Vertex
    var destination: Vertex
    override fun createInitialPaths(pq: State<LocatedEdgePath>) {
        createInitialPathsFrom(pq, start)
    }

    private fun identifyActualVertex(pe: PlanarizationEdge, d: Direction?, from: Boolean): Vertex {
        val cle = pe as ContainerLayoutEdge
        val und = if (from) cle.getFromConnected() else cle.getToConnected()
        return if (em.hasOuterCornerVertices(und!!)) {
            val c = und as Container?
            val cvs = em.getOuterCornerVertices(c!!)
            cvs.createVertex(getOrdForXDirection(d), getOrdForYDirection(d))
        } else {
            if (from) pe.getFrom() else pe.getTo()
        }
    }

    override fun isTerminationVertex(v: Int): Boolean {
        val candidate = p.vertexOrder[v]
        return candidate === destination
    }

    override fun allowConnectionsToContainerContents(): Boolean {
        return false
    }

    init {
        start = identifyActualVertex(ci, ci.getDrawDirection(), true)
        destination = identifyActualVertex(ci, reverse(ci.getDrawDirection()), false)
    }
}