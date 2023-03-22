package org.kite9.diagram.common.elements.vertex

import org.kite9.diagram.model.DiagramElement

/**
 * This is used where two edges are required to cross each other.  This vertex is added at the crossing
 * point to ensure that the planarization is 2d.
 *
 * @author robmoffat
 */
class EdgeCrossingVertex(val id: String, val underlyings: MutableSet<DiagramElement>) : AbstractVertex(id), MultiElementVertex {

    override fun hasDimension(): Boolean {
        return false
    }

    override fun isPartOf(de: DiagramElement?): Boolean {
        return underlyings.contains(de)
    }

    fun addUnderlying(de: DiagramElement) {
        underlyings.add(de)
    }

    fun removeUnderlying(de: DiagramElement) {
        underlyings.remove(de)
    }

    override fun getDiagramElements(): Set<DiagramElement> {
        return underlyings
    }
}