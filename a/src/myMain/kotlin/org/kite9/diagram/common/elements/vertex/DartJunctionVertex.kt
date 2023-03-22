package org.kite9.diagram.common.elements.vertex

import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.common.elements.vertex.AbstractVertex
import org.kite9.diagram.common.elements.vertex.MultiElementVertex

/**
 * A vertex modelling the join between a Connection and a Connected diagram element, created in the
 * process of giving a vertex a dimensioned shape in orthogonalization.
 *
 * @author robmoffat
 */
class DartJunctionVertex(id: String, val underlyings: Set<DiagramElement>) :
    AbstractVertex(id), MultiElementVertex {

    override fun getDiagramElements(): Set<DiagramElement> {
        return underlyings
    }

    override fun isPartOf(c: DiagramElement?): Boolean {
        return underlyings.contains(c)
    }
}