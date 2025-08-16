package org.kite9.diagram.dom.model

import org.kite9.diagram.dom.bridge.ElementContext
import org.kite9.diagram.dom.painter.LeafPainter
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Leaf
import org.kite9.diagram.model.style.ContentTransform
import org.w3c.dom.Element

/**
 * A fixed-size element on the diagram that can contain SVG sub-elements for rendering.
 *
 * @author robmoffat
 */
class ConnectedLeafImpl(
    el: Element,
    parent: DiagramElement,
    ctx: ElementContext,
    lo: LeafPainter,
    t: ContentTransform
) : AbstractConnectedRectangular(
    el, parent, ctx, lo, t
), Leaf