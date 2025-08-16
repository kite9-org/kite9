package org.kite9.diagram.model

import org.kite9.diagram.model.position.RectangleRenderingInformation
import org.kite9.diagram.model.style.ContainerPosition


/**
 * Positioned elements have an x,y position within a diagram.  If they also have width and height
 * they will be represented by a subclass of this, Rectangular.
 */
interface Positioned : DiagramElement {

    override fun getRenderingInformation(): RectangleRenderingInformation

    /**
     * Any other details about how this is to be positioned in its container.
     */
    fun getContainerPosition(): ContainerPosition?
}