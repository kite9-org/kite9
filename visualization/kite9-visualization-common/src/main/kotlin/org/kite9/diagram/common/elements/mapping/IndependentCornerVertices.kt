package org.kite9.diagram.common.elements.mapping

import org.kite9.diagram.model.DiagramElement

class IndependentCornerVertices(c: DiagramElement, depth: Int) :
    AbstractBaseCornerVertices(c, FULL_RANGE, FULL_RANGE, depth) {
    init {
        createInitialVertices(c)
    }
}