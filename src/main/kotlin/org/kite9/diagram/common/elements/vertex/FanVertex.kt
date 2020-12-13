package org.kite9.diagram.common.elements.vertex

import org.kite9.diagram.common.elements.vertex.AbstractVertex
import org.kite9.diagram.common.elements.vertex.NoElementVertex
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.Direction

/**
 * Special marker vertex that allows us to represent fan turns.
 *
 * @author robmoffat
 */
class FanVertex(id: String, val isInner: Boolean, val fanSides: List<Direction>) :
    AbstractVertex(id), NoElementVertex {

    override fun getDiagramElements(): Set<DiagramElement> {
        return emptySet()
    }

    override fun isPartOf(de: DiagramElement?): Boolean {
        return false
    }
}