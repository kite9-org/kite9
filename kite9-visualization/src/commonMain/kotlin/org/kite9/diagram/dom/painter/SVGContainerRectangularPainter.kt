package org.kite9.diagram.dom.painter

import org.kite9.diagram.dom.bridge.ElementContext
import org.kite9.diagram.dom.painter.DirectSVGGroupPainter
import org.w3c.dom.Element

/**
 * Handles painting for [DiagramElementType.CONTAINER]
 *
 * This implementation allows Containers to contain some SVG, but it cannot be used for sizing purposes.
 *
 * @author robmoffat
 */
class SVGContainerRectangularPainter(theElement: Element, ctx: ElementContext) : DirectSVGGroupPainter(theElement)