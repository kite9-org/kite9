package org.kite9.diagram.common.elements.mapping

import org.kite9.diagram.common.elements.grid.GridPositioner
import org.kite9.diagram.common.fraction.LongFraction
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.Direction

/**
 * Given original (user defined) diagram element, returns the planarization element(s) relating to it.
 */
interface ElementMapper {

    fun getGridPositioner(): GridPositioner

    fun requiresPlanarizationCornerVertices(c: DiagramElement): Boolean

}