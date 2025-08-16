package org.kite9.diagram.dom.model

import org.kite9.diagram.dom.bridge.ElementContext
import org.kite9.diagram.dom.css.CSSConstants
import org.kite9.diagram.dom.painter.LeafPainter
import org.kite9.diagram.dom.painter.Painter
import org.kite9.diagram.dom.transform.LeafTransformer
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.*
import org.kite9.diagram.model.position.BasicDimension2D
import org.kite9.diagram.model.position.CostedDimension2D
import org.kite9.diagram.model.position.Dimension2D
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.style.ContentTransform
import org.kite9.diagram.model.style.DiagramElementSizing
import org.kite9.diagram.model.style.HorizontalAlignment
import org.kite9.diagram.model.style.VerticalAlignment
import org.w3c.dom.Element
import kotlin.math.max

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
        return verticalAlignment
    }

    override fun getHorizontalAlignment(): HorizontalAlignment {
        ensureInitialized()
        return horizontalAlignment
    }

    override fun initialize() {
        super.initialize()
        initAlignment()
        initMinimumSize()
    }

    private fun initAlignment() {
        horizontalAlignment = ElementContext.getCssStyleEnumProperty<HorizontalAlignment>(CSSConstants.HORIZONTAL_ALIGNMENT, theElement, ctx)!!
        verticalAlignment = ElementContext.getCssStyleEnumProperty<VerticalAlignment>(CSSConstants.VERTICAL_ALIGNMENT, theElement, ctx)!!
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

    override fun getSize(within: Dimension2D): CostedDimension2D {
        if (this is Decal) {
            throw LogicException("Shouldn't be using size for decals")
        } else if (this is Container) {
            return ensureMinimumSize(sizeBasedOnPadding, within)
        } else if (this is Leaf) {
            val left = getPadding(Direction.LEFT)
            val right = getPadding(Direction.RIGHT)
            val up = getPadding(Direction.UP)
            val down = getPadding(Direction.DOWN)
            val bounds = leafBounds
            return ensureMinimumSize(BasicDimension2D(left + right + bounds.w, up + down + bounds.h), within)
        }
        throw LogicException("Not sure how to size: $this")
    }

    open fun ensureMinimumSize(c: Dimension2D, within: Dimension2D): CostedDimension2D {
        var min = (this as SizedRectangular).getMinimumSize()
        return CostedDimension2D(
            max(c.w, min.w),
            max(c.h, min.h), within
        )
    }

    private val leafBounds: Dimension2D
        get() {
            val p = painter
            return if (p is LeafPainter && transformer is LeafTransformer) {
                transformer.getBounds(p)
            } else CostedDimension2D.ZERO
        }

    override fun getXPathVariable(name: String): String? {
        if ("x0" == name || "x" == name) {
            return if (painter is LeafPainter) {
                var out = (painter as LeafPainter).bounds().x - getPadding(Direction.LEFT)
                "" + out
            } else {
                "0";
            }
        } else if ("y0" == name || "y" == name) {
            return if (painter is LeafPainter) {
                var out = (painter as LeafPainter).bounds().y - getPadding(Direction.UP)
                "" + out;
            } else {
                "0";
            }
        } else {
            return super.getXPathVariable(name);
        }
    }
}