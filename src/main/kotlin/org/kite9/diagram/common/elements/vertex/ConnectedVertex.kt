package org.kite9.diagram.common.elements.vertex

import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.style.ConnectionsSeparation

/**
 * Vertex to represent a single [Connected] element during planarization.
 *
 * @author robmoffat
 */
class ConnectedVertex(id: String, val underlying: Connected) : AbstractVertex(id), SingleElementVertex {

    override fun hasDimension(): Boolean {
        return true
    }

    override fun getOriginalUnderlying(): Connected {
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