package org.kite9.diagram.visualization.orthogonalization.vertex

import org.kite9.diagram.common.elements.vertex.AbstractVertex
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
class PortVertex(id: String, val port: Port) : AbstractVertex(id) {

    override fun getDiagramElements(): Set<DiagramElement> {
        return setOf(port)
    }

    override fun isPartOf(de: DiagramElement?): Boolean {
        return de == port
    }

    override var x: Double
        get() = super.x
        set(x) {
            super.x = x
            val pos = port.getRenderingInformation().position
            val y = pos?.h ?: 0.0
            port.getRenderingInformation().position = CostedDimension2D(x, y)
        }

    override var y: Double
        get() = super.y
        set(y) {
            super.y = y
            val pos = port.getRenderingInformation().position
            val x = pos?.w ?: 0.0
            port.getRenderingInformation().position = CostedDimension2D(x, y)
        }
}