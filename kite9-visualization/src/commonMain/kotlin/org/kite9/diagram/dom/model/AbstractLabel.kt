package org.kite9.diagram.dom.model

import org.kite9.diagram.dom.bridge.ElementContext
import org.kite9.diagram.dom.css.CSSConstants
import org.kite9.diagram.dom.painter.Painter
import org.kite9.diagram.model.Connection
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Label
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.End
import org.kite9.diagram.model.style.ContentTransform
import org.kite9.diagram.model.style.HorizontalAlignment
import org.kite9.diagram.model.style.VerticalAlignment
import org.w3c.dom.Element

abstract class AbstractLabel(
    el: Element,
    parent: DiagramElement,
    ctx: ElementContext,
    rp: Painter,
    t: ContentTransform,
    private val end: End?
) : AbstractCompactedRectangular(
    el, parent, ctx, rp, t
), Label, LayoutAligns {

    private var labelPlacement: Direction? = null

    override fun initialize() {
        super.initialize()
        labelPlacement = ElementContext.getCssStyleEnumProperty<Direction>(CSSConstants.DIRECTION, theElement, ctx)
    }

    override fun isConnectionLabel(): Boolean {
        return end != null
    }

    /**
     * Handles the case where labels can be nested inside terminators (this should be the way forward)
     * and also the old case where they were separate.
     */
    val connectionParent: Connection
        get() {
            ensureInitialized()
            var out: DiagramElement? = this
            do {
                out = out!!.getParent()
            } while (out !is Connection)
            return out
        }

    override fun getContainer(): Rectangular? {
        ensureInitialized()
        return if (isConnectionLabel()) {
            val c = connectionParent
            if (this === c.getFromLabel()) {
                c.getFrom().getContainer()
            } else if (this === c.getToLabel()) {
                c.getTo().getContainer()
            } else {
                throw ctx.contextualException("Couldn't get container for label " + getID(), theElement)
            }
        } else {
            super.getContainer()
        }
    }

    override fun getLabelPlacement(): Direction? {
        ensureInitialized()
        return labelPlacement
    }

    private var overrideHorizontalAlignment: HorizontalAlignment? = null
    private var overrideVerticalAlignment: VerticalAlignment? = null

    override fun getVerticalAlignment(): VerticalAlignment {
        return overrideVerticalAlignment ?: super.getVerticalAlignment()
    }

    override fun getHorizontalAlignment(): HorizontalAlignment {
        return overrideHorizontalAlignment ?: super.getHorizontalAlignment()
    }

    override fun setVerticalAlignment(va: VerticalAlignment) {
        overrideVerticalAlignment = va
    }

    override fun setHorizontalAlignment(ha: HorizontalAlignment) {
        overrideHorizontalAlignment = ha
    }

    override fun getEnd() : End? {
        return end
    }
}