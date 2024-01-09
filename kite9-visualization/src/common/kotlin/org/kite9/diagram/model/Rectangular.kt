package org.kite9.diagram.model

import org.kite9.diagram.model.position.RectangleRenderingInformation
import org.kite9.diagram.model.style.ContainerPosition

/**
 * Marker interface for diagram elements which consume a rectangular space, and therefore
 * return width and height in [RectangleRenderingInformation].
 *
 * @author robmoffat
 */
interface Rectangular : Positioned