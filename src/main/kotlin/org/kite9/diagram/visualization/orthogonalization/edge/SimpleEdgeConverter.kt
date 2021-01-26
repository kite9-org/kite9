package org.kite9.diagram.visualization.orthogonalization.edge

import org.kite9.diagram.common.elements.edge.BiDirectionalPlanarizationEdge
import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Direction.Companion.reverse
import org.kite9.diagram.visualization.orthogonalization.Dart
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization
import org.kite9.diagram.visualization.orthogonalization.contents.ContentsConverter
import org.kite9.diagram.visualization.planarization.mgt.BorderEdge

open class SimpleEdgeConverter(val cc: ContentsConverter) : EdgeConverter {

    override fun convertPlanarizationEdge(
        e: PlanarizationEdge,
        o: Orthogonalization,
        incident: Direction,
        externalVertex: Vertex,
        sideVertex: Vertex,
        planVertex: Vertex,
        fanStep: Direction?
    ): IncidentDart {
        val side = reverse(incident)
        o.createDart(sideVertex, externalVertex, createMap(e), side!!)
        return IncidentDart(externalVertex, sideVertex, side, e)
    }

    override fun buildDartsBetweenVertices(
        underlyings: Map<DiagramElement, Direction?>,
        o: Orthogonalization,
        end1: Vertex,
        end2: Vertex,
        d: Direction
    ): List<Dart> {
        var end1 = end1
        var end2 = end2
        val start: MutableList<Dart> = ArrayList()
        val end: MutableList<Dart> = ArrayList()
        var cont = true
        while (cont) {
            val end1Leaver = getDartGoing(end1, d)
            val end2Leaver = getDartGoing(end2, reverse(d))
            cont = false
            if (end1Leaver != null) {
                // add underlyings
                start.add(
                    o.createDart(
                        end1Leaver.getFrom(),
                        end1Leaver.getTo(),
                        underlyings,
                        end1Leaver.getDrawDirection()
                    )
                )
                cont = true
                end1 = end1Leaver.otherEnd(end1)
            }
            if (end2Leaver != null && end2Leaver !== end1Leaver) {
                // add underlyings
                end.add(
                    o.createDart(
                        end2Leaver.getFrom(),
                        end2Leaver.getTo(),
                        underlyings,
                        end2Leaver.getDrawDirection()
                    )
                )
                cont = true
                end2 = end2Leaver.otherEnd(end2)
            }
            if (end1 === end2) {
                cont = false
            }
        }
        if (end1 !== end2) {
            start.add(o.createDart(end1, end2, underlyings, d))
        }
        end.reverse()
        start.addAll(end)
        return start
    }

    private fun getDartGoing(end1: Vertex, d: Direction?): Dart? {
        for (e in end1.getEdges()) {
            if (e is Dart && e.getDrawDirectionFrom(end1) === d) {
                return e
            }
        }
        return null
    }

    protected fun createMap(e: PlanarizationEdge): Map<DiagramElement, Direction?> {
        return (e as? BorderEdge)?.getDiagramElements()
            ?: if (e is BiDirectionalPlanarizationEdge) {
                mapOf(e.getOriginalUnderlying() to null)
            } else {
                throw LogicException()
            }
    }
}