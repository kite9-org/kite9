package org.kite9.diagram.model

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.model.style.Placement

interface PlacementPositioned : Positioned {

    /**
     * The position of this element is represented by a Placement
     */
    override fun getContainerPosition(d: Dimension): Placement
}