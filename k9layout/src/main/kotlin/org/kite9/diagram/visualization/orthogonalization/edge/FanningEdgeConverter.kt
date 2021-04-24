package org.kite9.diagram.visualization.orthogonalization.edge

import org.kite9.diagram.common.elements.edge.BiDirectionalPlanarizationEdge
import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.common.elements.mapping.ElementMapper
import org.kite9.diagram.common.elements.vertex.FanVertex
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.model.*
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Direction.Companion.reverse
import org.kite9.diagram.model.style.DiagramElementSizing
import org.kite9.diagram.visualization.orthogonalization.Dart
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
        if ((fan != null) && (elementNeedsFan(incident, sideVertex.getDiagramElements()))) {
            // disregard for straight edges
            if (e.getDrawDirection() == null || isUnderlyingContradicting(e)) {
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
        } else if (portNeedsFan(sideVertex, e)) {
            val toJoin = if (fan != null) {
                val fanOuter: Vertex =
                    FanVertex(planVertex.getID() + "-fo-" + counter, false, listOf(incident, reverse(fan)!!))
                val fanInner: Vertex =
                    FanVertex(planVertex.getID() + "-fi-" + counter, true, listOf(incident, fan!!))
                counter++
                val map = createMap(e)
                o.createDart(externalVertex, fanOuter, map, incident)
                o.createDart(fanOuter, fanInner, map, reverse(fan)!!)
                fanInner
            } else {
                externalVertex
            }

            val chainEnd = getOutermostVertex(sideVertex, reverse(incident)!!)
            val out = super.convertPlanarizationEdge(e, o, incident, toJoin, chainEnd, planVertex, fan)
            out.external = externalVertex
            return out
        }
        return super.convertPlanarizationEdge(e, o, incident, externalVertex, sideVertex, planVertex, null)
    }

    protected fun getOutermostVertex(from: Vertex, d: Direction) : Vertex {
        var at = from;
        do {
            val dart = at.getEdges()
                .filterIsInstance<Dart>()
                .filter { it.getDrawDirectionFrom(at) == d}
                .firstOrNull()

            if (dart == null) {
                return at;
            } else {
                at = dart.otherEnd(at)
            }
        } while (true)
    }

    protected fun portNeedsFan(sideVertex: Vertex, e: PlanarizationEdge) : Boolean {
        return (sideVertex.getDiagramElements().filterIsInstance<Port>().isNotEmpty()) &&
                (e.getDiagramElements().keys.filterIsInstance<Connection>().isNotEmpty())
    }

    protected fun elementNeedsFan(incident: Direction, diagramElements: Set<DiagramElement>): Boolean {
        val underlying = diagramElements.filterIsInstance<SizedRectangular>().firstOrNull()
        return if (underlying == null) {
            false
        } else {
            underlying.getSizing(!Direction.isHorizontal(incident)) != DiagramElementSizing.MAXIMIZE
        }
    }

    override fun getDefaultLabelDirection(incident: Direction, fan: Direction?): Direction {
        if (fan != null) {
            return reverse(fan)!!
        } else {
            return super.getDefaultLabelDirection(incident, fan)
        }
    }
}