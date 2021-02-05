package org.kite9.diagram.visualization.orthogonalization

import org.kite9.diagram.common.elements.mapping.ConnectionEdge
import org.kite9.diagram.common.elements.vertex.AbstractVertex
import org.kite9.diagram.common.elements.vertex.SingleElementVertex
import org.kite9.diagram.model.Connection
import org.kite9.diagram.model.DiagramElement

/**
 * Models a bend within an Edge.  Darts are horizontal or vertical, so this allows
 * for the modelling of the joins between darts in the Edge's path.
 *
 * @author robmoffat
 */
class ConnectionEdgeBendVertex(name: String, underlying: ConnectionEdge) : AbstractVertex(name), SingleElementVertex {

    private val underlying: Connection

    override fun getOriginalUnderlying(): DiagramElement {
        return underlying
    }

    override fun isPartOf(de: DiagramElement?): Boolean {
        return underlying === de
    }

    override fun getDiagramElements(): Set<DiagramElement> {
        return setOf(underlying)
    }

    init {
        this.underlying = underlying.getOriginalUnderlying()
    }
}