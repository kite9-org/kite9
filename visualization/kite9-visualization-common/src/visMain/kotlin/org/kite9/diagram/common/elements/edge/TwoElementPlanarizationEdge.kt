package org.kite9.diagram.common.elements.edge

import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.Direction

/**
 * Used at the interface between two diagram elements (typically, in a grid).
 * @author robmoffat
 */
interface TwoElementPlanarizationEdge : PlanarizationEdge {

    fun getElementForSide(d: Direction): DiagramElement?
}