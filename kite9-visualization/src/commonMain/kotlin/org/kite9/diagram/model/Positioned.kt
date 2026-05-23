package org.kite9.diagram.model

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.model.position.RectangleRenderingInformation
import org.kite9.diagram.model.style.RectangularPosition


/**
 * Positioned elements have an x,y position within a diagram.  If they also have width and height
 * they will be represented by a subclass of this, Rectangular.
 */
interface Positioned : DiagramElement {

    override fun getRenderingInformation(): RectangleRenderingInformation

    /**
     * Any other details about how this is to be positioned in its container, in a given
     * dimension.
     */
    fun getContainerPosition(d: Dimension): RectangularPosition?
}