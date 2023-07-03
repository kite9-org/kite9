package org.kite9.diagram.common.elements.vertex

import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.ConnectedRectangular
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.style.ConnectionsSeparation

/**
 * Vertex to represent a single [ConnectedRectangular] element during planarization.
 *
 * @author robmoffat
 */
class ConnectedRectangularVertex(id: String, val underlying: ConnectedRectangular) : AbstractVertex(id), SingleElementVertex {

    override fun hasDimension(): Boolean {
        return true
    }

    override fun getOriginalUnderlying(): ConnectedRectangular {
        return underlying
    }

    override fun isPartOf(de: DiagramElement?): Boolean {
        return underlying === de
    }

    override fun getDiagramElements(): Set<DiagramElement> {
        return setOf(underlying)
    }

    /**
     * Means that when we try to lay out, we arrange so that different incoming connections
     * are on opposite sides.
     */
    fun isSeparatingConnections(): Boolean {
        return underlying.getConnectionsSeparationApproach() == ConnectionsSeparation.SEPARATE
    }
}