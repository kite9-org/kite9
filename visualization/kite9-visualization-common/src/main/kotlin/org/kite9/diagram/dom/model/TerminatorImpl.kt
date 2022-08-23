package org.kite9.diagram.dom.model

import org.kite9.diagram.dom.bridge.ElementContext
import org.kite9.diagram.dom.css.CSSConstants
import org.kite9.diagram.dom.painter.Painter
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Connection
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Terminator
import org.kite9.diagram.model.position.CostedDimension2D
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

    private var markerReserve = 0.0
    private var arrivalSide: Direction? = null
    private var end: End? = null

    override fun initialize() {
        super.initialize()
        arrivalSide = ctx.getCssStyleEnumProperty(CSSConstants.ARRIVAL_SIDE, theElement, Direction::class)
        end = ctx.getCssStyleEnumProperty(CSSConstants.LINK_END, theElement, End::class)
        markerReserve = ctx.getCssStyleDoubleProperty(CSSConstants.MARKER_RESERVE, theElement)
    }

    override fun getContainer(): Container? {
        val c = getConnection()
        return when {
            this === c.getFromDecoration() -> {
                c.getFrom().getContainer()
            }
            this === c.getToDecoration() -> {
                c.getTo().getContainer()
            }
            else -> {
                throw ctx.contextualException("Couldn't get container for terminator " + getID(), theElement)
            }
        }
    }

    override fun getReservedLength(): Double {
        ensureInitialized()
        return getPadding(Direction.RIGHT) + getMargin(Direction.RIGHT)
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

    override fun getConnection(): Connection {
        ensureInitialized()
        var parent = getParent()
        while ((parent !is Connection) && (parent != null)) {
            parent = getParent()
        }
        if (parent is Connection) {
            return parent
        } else {
            throw ctx.contextualException("Parent is not link for terminator", theElement)
        }
    }

    override fun getEnd(): End {
        ensureInitialized()
        return end!!
    }

    override fun getSize(within: Dimension2D): CostedDimension2D {
        throw LogicException("Shouldn't be using size for terminators")
    }

    override fun getSizing(horiz: Boolean): DiagramElementSizing {
        return DiagramElementSizing.MINIMIZE
    }

    companion object {
        private fun attributesMatch(name: String, a: TerminatorImpl, b: TerminatorImpl): Boolean {
            return a.theElement.getAttribute(name) ==
                    b.theElement.getAttribute(name)
        }
    }
}