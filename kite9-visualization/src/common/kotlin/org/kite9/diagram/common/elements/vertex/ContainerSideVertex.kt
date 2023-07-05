package org.kite9.diagram.common.elements.vertex

import org.kite9.diagram.model.DiagramElement

/**
 * This is used by [ContainerConnectionTransform2], which splits edges off from MultiCornerVertex points
 * onto their own vertices.
 *
 * @author robmoffat
 */
class ContainerSideVertex(val id: String) : AbstractVertex(id), MultiElementVertex {

    private val underlyings : MutableSet<DiagramElement> = hashSetOf()

    override fun hasDimension(): Boolean {
        return false
    }

    override fun isPartOf(de: DiagramElement?): Boolean {
        return underlyings.contains(de)
    }

    fun addUnderlying(de: DiagramElement) {
        underlyings.add(de)
    }

    override fun getDiagramElements(): Set<DiagramElement> {
        return underlyings
    }
}