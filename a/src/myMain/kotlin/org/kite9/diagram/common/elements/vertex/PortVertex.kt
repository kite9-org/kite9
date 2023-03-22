package org.kite9.diagram.common.elements.vertex

import org.kite9.diagram.common.fraction.LongFraction
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Port
import org.kite9.diagram.model.position.CostedDimension2D

/**
 * Special marker vertex that allows us to represent the join points for
 * darts/vertices constructed by the VertexArranger.
 *
 * This keeps track of the underlying PLanarizationEdge that needs to meet from
 * the OrthBuilder process.
 *
 * @author robmoffat
 */
class PortVertex(id: String, xOrdinal: LongFraction, yOrdinal: LongFraction, val port: Port) : MultiCornerVertex(id, xOrdinal, yOrdinal) {

    override fun getDiagramElements(): Set<DiagramElement> {
        return setOf(port)
    }

    override fun isPartOf(de: DiagramElement?): Boolean {
        return de == port
    }
}