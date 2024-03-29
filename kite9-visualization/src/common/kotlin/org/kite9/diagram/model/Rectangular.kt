package org.kite9.diagram.model

import org.kite9.diagram.model.position.RectangleRenderingInformation
import org.kite9.diagram.model.style.ContainerPosition

/**
 * Marker interface for diagram elements which consume a rectangular space, and therefore
 * return [RectangleRenderingInformation].
 *
 * @author robmoffat
 */
interface Rectangular : DiagramElement {

    override fun getRenderingInformation(): RectangleRenderingInformation

    /**
     * Any other details about how this rectangular is to be positioned in the container.
     */
    fun getContainerPosition(): ContainerPosition?
}