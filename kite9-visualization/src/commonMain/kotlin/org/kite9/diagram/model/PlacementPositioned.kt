package org.kite9.diagram.model

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.model.style.MeasuredRectangularPosition

/**
 * Positioned elements where there are instructions on how the object is placed within
 * the rectangular container that houses it.
 */
interface PlacementPositioned : Positioned {

    /**
     * The position of this element is represented by a Placement
     */
    override fun getContainerPosition(d: Dimension): MeasuredRectangularPosition
}