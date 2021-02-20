package org.kite9.diagram.dom.model

import org.kite9.diagram.dom.bridge.ElementContext
import org.kite9.diagram.dom.css.CSSConstants
import org.kite9.diagram.dom.painter.Painter
import org.kite9.diagram.model.Connection
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Label
import org.kite9.diagram.model.position.End
import org.kite9.diagram.model.style.ContentTransform
import org.kite9.diagram.model.style.LabelPlacement
import org.w3c.dom.Element

abstract class AbstractLabel(
    el: Element,
    parent: DiagramElement,
    ctx: ElementContext,
    rp: Painter,
    t: ContentTransform
) : AbstractCompactedRectangular(
    el, parent, ctx, rp, t
), Label {

    private var end: End? = null
    private var labelPlacement: LabelPlacement? = null

    override fun initialize() {
        super.initialize()
        end = ctx.getCSSStyleEnumProperty(CSSConstants.LINK_END, theElement) as End?
        labelPlacement = ctx.getCSSStyleEnumProperty(CSSConstants.LABEL_PLACEMENT, theElement) as LabelPlacement?
    }

    override fun isConnectionLabel(): Boolean {
        ensureInitialized()
        return getParent() !is Container
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

    override fun getContainer(): Container? {
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

    override fun getEnd(): End? {
        ensureInitialized()
        return end
    }

    override fun getLabelPlacement(): LabelPlacement? {
        ensureInitialized()
        return labelPlacement
    }
}