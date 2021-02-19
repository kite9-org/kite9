package org.kite9.diagram.dom.model

import org.kite9.diagram.dom.bridge.ElementContext
import org.kite9.diagram.dom.css.CSSConstants
import org.kite9.diagram.dom.painter.Painter
import org.kite9.diagram.model.AlignedRectangular
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.SizedRectangular
import org.kite9.diagram.model.position.BasicDimension2D
import org.kite9.diagram.model.position.Dimension2D
import org.kite9.diagram.model.style.ContentTransform
import org.kite9.diagram.model.style.DiagramElementSizing
import org.kite9.diagram.model.style.HorizontalAlignment
import org.kite9.diagram.model.style.VerticalAlignment
import org.w3c.dom.Element

abstract class AbstractCompactedRectangular(
    el: Element,
    parent: DiagramElement?,
    ctx: ElementContext,
    rp: Painter,
    t: ContentTransform
) : AbstractRectangular(
    el, parent, ctx, rp, t
), SizedRectangular, AlignedRectangular {

    private var verticalAlignment: VerticalAlignment = VerticalAlignment.CENTER
    private var horizontalAlignment: HorizontalAlignment = HorizontalAlignment.CENTER
    private var minimumSize: Dimension2D? = null

    override fun getVerticalAlignment(): VerticalAlignment {
        ensureInitialized()
        return verticalAlignment!!
    }

    override fun getHorizontalAlignment(): HorizontalAlignment {
        ensureInitialized()
        return horizontalAlignment!!
    }

    override fun initialize() {
        super.initialize()
        initAlignment()
        initMinimumSize()
    }

    private fun initAlignment() {
        horizontalAlignment =
            ctx.getCSSStyleEnumProperty(CSSConstants.HORIZONTAL_ALIGNMENT, theElement) as HorizontalAlignment
        verticalAlignment = ctx.getCSSStyleEnumProperty(CSSConstants.VERTICAL_ALIGNMENT, theElement) as VerticalAlignment
    }

    private fun initMinimumSize() {
        val w = getCssDoubleValue(CSSConstants.RECT_MINIMUM_WIDTH)
        val h = getCssDoubleValue(CSSConstants.RECT_MINIMUM_HEIGHT)
        minimumSize = BasicDimension2D(w, h)
    }

    override fun getMinimumSize(): Dimension2D {
        ensureInitialized()
        return minimumSize!!
    }

    override fun getSizing(horiz: Boolean): DiagramElementSizing {
        ensureInitialized()
        return if (horiz) sizingHoriz else sizingVert
    }
}