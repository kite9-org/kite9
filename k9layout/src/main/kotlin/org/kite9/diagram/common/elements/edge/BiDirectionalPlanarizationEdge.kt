package org.kite9.diagram.common.elements.edge

import org.kite9.diagram.model.DiagramElement

interface BiDirectionalPlanarizationEdge : PlanarizationEdge {

    fun getOriginalUnderlying(): DiagramElement
    var fromUnderlying: DiagramElement?
    var toUnderlying: DiagramElement?
}