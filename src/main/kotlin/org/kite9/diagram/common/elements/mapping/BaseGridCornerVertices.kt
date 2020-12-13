package org.kite9.diagram.common.elements.mapping

import org.kite9.diagram.model.DiagramElement

class BaseGridCornerVertices(container: DiagramElement, depth: Int) :
    AbstractBaseCornerVertices(container, FULL_RANGE, FULL_RANGE, depth) {

    override fun getVertexIDStem(): String {
        return super.getVertexIDStem() + "-g"
    }

    fun getGridContainer(): DiagramElement {
        return rootContainer
    }

    init {
        createInitialVertices(null)
    }
}