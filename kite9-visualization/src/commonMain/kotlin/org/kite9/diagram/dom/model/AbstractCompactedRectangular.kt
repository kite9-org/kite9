package org.kite9.diagram.dom.model

import org.kite9.diagram.common.elements.Dimension
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
import org.kite9.diagram.model.style.Measurement
import org.kite9.diagram.model.style.Placement
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
    protected var alignments: Array<Placement> = emptyArray()

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
        initConnectionAlignment()
    }

    protected fun initConnectionAlignment() {
        alignments = arrayOf(
            ctx.getCssStylePlacementProperty(CSSConstants.HORIZONTAL_ALIGN_POSITION, theElement),
            ctx.getCssStylePlacementProperty(CSSConstants.VERTICAL_ALIGN_POSITION, theElement)
        )
    }

    private fun initAlignment() {
        horizontalAlignment = ElementContext.getCssStyleEnumProperty<HorizontalAlignment>(CSSConstants.HORIZONTAL_ALIGNMENT, theElement, ctx)!!
        verticalAlignment = ElementContext.getCssStyleEnumProperty<VerticalAlignment>(CSSConstants.VERTICAL_ALIGNMENT, theElement, ctx)!!
    }

    private fun initMinimumSize() {

        fun maxInPositionedDimension(d: Dimension) : Double? {
            return getContents()
                .filterIsInstance<PlacementPositioned>()
                .map { it.getContainerPosition(d) }
                .filter { it.type == Measurement.PIXELS }
                .map { it.amount }
                .maxOrNull()
        }

        val w = getCssDoubleValue(CSSConstants.RECT_MINIMUM_WIDTH)
        val h = getCssDoubleValue(CSSConstants.RECT_MINIMUM_HEIGHT)

        val pLeft = getPadding(Direction.LEFT)
        val pRight = getPadding(Direction.RIGHT)
        val pUp = getPadding(Direction.UP)
        val pDown = getPadding(Direction.DOWN)

        val w2 = pLeft + pRight + contentBounds.w
        val h2 = pUp + pDown + contentBounds.h

        val wPort = maxInPositionedDimension(Dimension.H) ?: 0.0
        val hPort = maxInPositionedDimension(Dimension.V) ?: 0.0

        minimumSize = BasicDimension2D(
            doubleArrayOf(w, w2, wPort).max(),
            doubleArrayOf(h, h2, hPort).max())
    }

    override fun getConnectionAlignment(d: Dimension): Placement {
        ensureInitialized()
        return alignments[d.ordinal]
    }

    override fun getMinimumSize(): Dimension2D {
        ensureInitialized()
        return minimumSize!!
    }

    override fun getSizing(horiz: Boolean): DiagramElementSizing {
        ensureInitialized()
        return if (horiz) sizingHoriz else sizingVert
    }

    private val contentBounds: Dimension2D
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