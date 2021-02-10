package org.kite9.diagram.dom.model

import org.kite9.diagram.dom.bridge.ElementContext
import org.kite9.diagram.dom.css.CSSConstants
import org.kite9.diagram.dom.painter.Painter
import org.kite9.diagram.model.Connection
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Terminator
import org.kite9.diagram.model.position.CostedDimension2D.Companion.ZERO
import org.kite9.diagram.model.position.Dimension2D
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.End
import org.kite9.diagram.model.style.ContentTransform
import org.kite9.diagram.model.style.DiagramElementSizing
import org.w3c.dom.Element

class TerminatorImpl(
    el: Element,
    parent: DiagramElement,
    ctx: ElementContext,
    rp: Painter,
    t: ContentTransform
) : AbstractRectangular(
    el, parent, ctx, rp, t
), Terminator {

    private var reference: String? = null
    private var markerReserve = 0.0
    private var arrivalSide: Direction? = null
    private var end: End? = null

    override fun initialize() {
        super.initialize()
        arrivalSide = ctx.getCSSStyleProperty(CSSConstants.ARRIVAL_SIDE, theElement) as Direction?
        end = ctx.getCSSStyleProperty(CSSConstants.LINK_END, theElement) as End?
        val from = end === End.FROM
        reference =
            if (from) ctx.getCssStringValue(CSSConstants.MARKER_START_REFERENCE, theElement) else ctx.getCssStringValue(
                CSSConstants.MARKER_END_REFERENCE, theElement
            )
        markerReserve = ctx.getCssDoubleValue(CSSConstants.MARKER_RESERVE, theElement)
    }

    override fun getContainer(): Container? {
        val c = getParent() as Connection
        return if (this === c.getFromDecoration()) {
            c.getFrom().getContainer()
        } else if (this === c.getToDecoration()) {
            c.getTo().getContainer()
        } else {
            throw ctx.contextualException("Couldn't get container for terminator " + getID(), theElement)
        }
    }

    override fun getReservedLength(): Double {
        ensureInitialized()
        return getPadding(Direction.RIGHT) + getMargin(Direction.RIGHT)
    }

    val markerUrl: String?
        get() {
            ensureInitialized()
            return if (reference != null) {
                "url(#$reference)"
            } else {
                null
            }
        }

    override fun getMarkerReserve(): Double {
        ensureInitialized()
        return markerReserve
    }

    override fun styleMatches(t2: Terminator?): Boolean {
        return if (t2 is TerminatorImpl) {
            val styleMatch = attributesMatch("style", this, t2)
            val classMatch = attributesMatch("class", this, t2)
            styleMatch && classMatch
        } else {
            false
        }
    }

    override fun getMinimumSize(): Dimension2D {
        return ZERO
    }

    override fun getXPathVariable(name: String): String? {
        if ("x0" == name || "y0" == name) {
            return "0"
        } else if ("x1" == name || "width" == name) {
            return "" + sizeBasedOnPadding.width()
        } else if ("y1" == name || "height" == name) {
            return "" + sizeBasedOnPadding.height()
        }
        return null
    }

    override fun getArrivalSide(): Direction? {
        ensureInitialized()
        return arrivalSide
    }

    /**
     * This is currently true, but won't always be.
     */
    override fun getConnection(): Connection {
        ensureInitialized()
        return getParent() as Connection
    }

    override fun getEnd(): End {
        ensureInitialized()
        return end!!
    }

    override fun getSizing(horiz: Boolean): DiagramElementSizing {
        return DiagramElementSizing.MINIMIZE
    }

    companion object {
        private fun attributesMatch(name: String, a: TerminatorImpl, b: TerminatorImpl): Boolean {
            return a.dOMElement.getAttribute(name) ==
                    b.dOMElement.getAttribute(name)
        }
    }
}