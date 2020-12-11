package org.kite9.diagram.common.elements.edge

import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.DiagramElement

interface BiDirectionalPlanarizationEdge : PlanarizationEdge {

    fun getOriginalUnderlying(): DiagramElement
    fun getFromConnected(): Connected
    fun getToConnected(): Connected
}