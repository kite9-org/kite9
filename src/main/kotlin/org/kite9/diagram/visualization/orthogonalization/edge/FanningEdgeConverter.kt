package org.kite9.diagram.visualization.orthogonalization.edge

import org.kite9.diagram.common.elements.edge.BiDirectionalPlanarizationEdge
import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.common.elements.mapping.ElementMapper
import org.kite9.diagram.common.elements.vertex.FanVertex
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Direction.Companion.reverse
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization
import org.kite9.diagram.visualization.orthogonalization.contents.ContentsConverter
import org.kite9.diagram.visualization.planarization.Tools.Companion.isUnderlyingContradicting

class FanningEdgeConverter(cc: ContentsConverter, em: ElementMapper) : LabellingEdgeConverter(cc, em) {

    private var counter = 0

    override fun convertPlanarizationEdge(
        e: PlanarizationEdge,
        o: Orthogonalization,
        incident: Direction,
        externalVertex: Vertex,
        sideVertex: Vertex,
        planVertex: Vertex,
        fan: Direction?
    ): IncidentDart {
        if (fan != null) {

            // disregard for straight edges
            if (e.getDrawDirection() == null || isUnderlyingContradicting(e)) {
                val c =
                    if (incident === e.getDrawDirection()) (e as BiDirectionalPlanarizationEdge).getToConnected() else (e as BiDirectionalPlanarizationEdge).getFromConnected()
                val fanOuter: Vertex =
                    FanVertex(planVertex.getID() + "-fo-" + counter, false, listOf(incident, reverse(fan)!!))
                val fanInner: Vertex =
                    FanVertex(planVertex.getID() + "-fi-" + counter, true, listOf(incident, fan!!))
                counter++
                val map = createMap(e)
                o.createDart(externalVertex, fanOuter, map, incident)
                o.createDart(fanOuter, fanInner, map, reverse(fan)!!)
                val out = super.convertPlanarizationEdge(e, o, incident, fanInner, sideVertex, planVertex, fan)
                out.external = externalVertex
                return out
            }
        }
        return super.convertPlanarizationEdge(e, o, incident, externalVertex, sideVertex, planVertex, fan)
    }
}