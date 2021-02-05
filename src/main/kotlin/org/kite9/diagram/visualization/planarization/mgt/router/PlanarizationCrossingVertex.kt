package org.kite9.diagram.visualization.planarization.mgt.router

import org.kite9.diagram.common.elements.vertex.AbstractVertex
import org.kite9.diagram.common.elements.vertex.NoElementVertex
import org.kite9.diagram.model.DiagramElement

/**
 * This is used where a routing goes from one side of the planarization axis to the other.
 *
 * @author robmoffat
 */
class PlanarizationCrossingVertex(id: String) : AbstractVertex(id), NoElementVertex {

    fun getOriginalUnderlying(): DiagramElement? {
        return null
    }

    override fun isPartOf(de: DiagramElement?): Boolean {
        return false
    }

    override fun getDiagramElements(): Set<DiagramElement> {
        return emptySet()
    }
}