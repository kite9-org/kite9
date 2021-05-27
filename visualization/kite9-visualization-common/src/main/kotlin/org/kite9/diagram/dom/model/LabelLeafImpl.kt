package org.kite9.diagram.dom.model

import org.kite9.diagram.dom.bridge.ElementContext
import org.kite9.diagram.dom.painter.LeafPainter
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Label
import org.kite9.diagram.model.Leaf
import org.kite9.diagram.model.style.ContentTransform
import org.w3c.dom.Element

/**
 * For text or shape-based labels within the diagram.
 *
 * @author robmoffat
 */
class LabelLeafImpl(
    el: Element,
    parent: DiagramElement,
    ctx: ElementContext,
    lo: LeafPainter,
    t: ContentTransform
) : AbstractLabel(
    el, parent, ctx, lo, t
), Label, Leaf