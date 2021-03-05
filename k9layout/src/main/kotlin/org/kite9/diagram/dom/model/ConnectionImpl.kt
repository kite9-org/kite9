package org.kite9.diagram.dom.model

import org.kite9.diagram.common.BiDirectional
import org.kite9.diagram.dom.bridge.ElementContext
import org.kite9.diagram.dom.css.CSSConstants
import org.kite9.diagram.dom.painter.Painter
import org.kite9.diagram.dom.painter.RoutePainterImpl
import org.kite9.diagram.dom.processors.xpath.XPathAware
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.*
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Direction.Companion.reverse
import org.kite9.diagram.model.position.End
import org.kite9.diagram.model.position.RouteRenderingInformation
import org.kite9.diagram.model.position.RouteRenderingInformationImpl
import org.kite9.diagram.model.style.ContentTransform
import org.w3c.dom.Element
import org.w3c.dom.NodeList

class ConnectionImpl(
    el: Element,
    parent: DiagramElement?,
    ctx: ElementContext,
    p: Painter,
    t: ContentTransform
) : AbstractModelDiagramElement(
    el, parent, ctx, p, t
), Connection, XPathAware {

    private var fromId: String? = null
    private var toId: String? = null

    override fun initialize() {
        super.initialize()
        initReferences()
        initFromTo()
        initContents()
        initDrawDirection()
        initRank()
        initSize()
    }

    private fun initReferences() {
        fromId = getReference(CSSConstants.LINK_FROM_XPATH)
        toId = getReference(CSSConstants.LINK_TO_XPATH)
    }

    /**
     * For elements which are containers, call this method as part of initialize.
     */
    override fun initContents(): MutableList<DiagramElement> {
        val contents: MutableList<DiagramElement> = mutableListOf()
        for (de in ctx.getChildDiagramElements(this)) {
            if (de is Terminator) {
                val e = de.getEnd()
                if (e === End.FROM) {
                    fromDecoration = if (fromDecoration == null) de else fromDecoration
                } else if (e === End.TO) {
                    toDecoration = if (toDecoration == null) de else toDecoration
                } else if (fromDecoration == null) {
                    fromDecoration = de
                } else if (toDecoration == null) {
                    toDecoration = de
                }
            } else if (de is Label) {
                val e = de.getEnd()
                if (e === End.FROM) {
                    fromLabel = if (fromLabel == null) de else fromLabel
                } else if (e === End.TO) {
                    toLabel = if (toLabel == null) de else toLabel
                } else if (fromLabel == null) {
                    fromLabel = de
                } else if (toLabel == null) {
                    toLabel = de
                }
            }
        }
        return contents
    }

    protected fun initSize() {
        minimumLength = ctx.getCssStyleDoubleProperty(CSSConstants.LINK_MINIMUM_LENGTH, theElement)
        cornerRadius = ctx.getCssStyleDoubleProperty(CSSConstants.LINK_CORNER_RADIUS, theElement)
    }

    protected fun initRank() {
        val rank = theElement.getAttribute("rank")
        if ("" != rank) {
            this.rank = rank?.toInt() ?: 0
        } else {
            // if rank isn't set, then connections are ranked in order from last to first..
            this.rank = indexOf(theElement, theElement.parentNode!!.childNodes)
        }
    }

    protected fun initFromTo() {
        from = ctx.getReferencedElement(fromId!!, theElement) as Connected?
        to = ctx.getReferencedElement(toId!!, theElement) as Connected?
        if (from == null) {
            throw ctx.contextualException("Couldn't resolve 'from' reference for " + getID(), theElement)
        }
        if (to == null) {
            throw ctx.contextualException("Couldn't resolve 'to' reference for " + getID(), theElement)
        }
    }

    private fun initDrawDirection() {
        drawDirection = ctx.getCSSStyleEnumProperty(CSSConstants.CONNECTION_DIRECTION, theElement, Direction::class)
    }

    private fun indexOf(e: Element, within: NodeList): Int {
        for (i in 0 until within.length) {
            if (within.item(i) === e) {
                return i
            }
        }
        return -1
    }

    private fun getReference(css: String): String? {
        return ctx.getReference(css, theElement)
    }

    private var from: Connected? = null
    private var to: Connected? = null
    private var drawDirection: Direction? = null
    private var fromDecoration: Terminator? = null
    private var toDecoration: Terminator? = null
    private var fromLabel: Label? = null
    private var toLabel: Label? = null
    private var rank = 0
    private var minimumLength = 0.0
    private var cornerRadius = 0.0

    override fun getFrom(): Connected {
        ensureInitialized()
        return from!!
    }

    override fun getTo(): Connected {
        ensureInitialized()
        return to!!
    }

    override fun otherEnd(end: Connected): Connected {
        return if (end === getFrom()) {
            getTo()
        } else if (end === getTo()) {
            getFrom()
        } else {
            throw ctx.contextualException("otherEnd of neither from or to $this $end", theElement)
        }
    }

    override fun meets(e: BiDirectional<Connected>?): Boolean {
        return meets(e!!.getFrom()) || meets(e.getTo())
    }

    override fun meets(v: Connected): Boolean {
        return getFrom() === v || getTo() === v
    }

    override fun getDrawDirection(): Direction? {
        ensureInitialized()
        return drawDirection
    }

    override fun getDrawDirectionFrom(from: Connected): Direction? {
        return if (getFrom() === from) {
            getDrawDirection()
        } else {
            reverse(getDrawDirection())
        }
    }

    override fun getFromDecoration(): Terminator {
        ensureInitialized()
        return fromDecoration!!
    }

    override fun getToDecoration(): Terminator {
        ensureInitialized()
        return toDecoration!!
    }

    override fun getFromLabel(): Label? {
        ensureInitialized()
        return fromLabel
    }

    override fun getToLabel(): Label? {
        ensureInitialized()
        return toLabel
    }

    private val ri: RouteRenderingInformation = RouteRenderingInformationImpl()


    override fun getRenderingInformation(): RouteRenderingInformation {
        return ri
    }

    override fun getRank(): Int {
        ensureInitialized()
        return rank
    }

    override fun getMargin(d: Direction): Double {
        ensureInitialized()
        return margin[d.ordinal]
    }

    override fun getPadding(d: Direction): Double {
        ensureInitialized()
        return padding[d.ordinal]
    }

    override fun getXPathVariable(name: String): String? {
        ensureInitialized()
        if ("path" == name) {
            val routePainter = RoutePainterImpl()
            val startReserve: Double = if (fromDecoration == null) 0.0 else fromDecoration!!.getMarkerReserve()
            val endReserve: Double = if (toDecoration == null) 0.0 else toDecoration!!.getMarkerReserve()
            val gp = routePainter.drawRouting(
                getRenderingInformation(),
                RoutePainterImpl.ReservedLengthEndDisplayer(startReserve),
                RoutePainterImpl.ReservedLengthEndDisplayer(endReserve),
                RoutePainterImpl.CurvedCornerHopDisplayer(getCornerRadius().toFloat()), false
            )
            return gp.toString()
        } else if ("markerstart" == name) {
            if (fromDecoration is TerminatorImpl) {
                return (fromDecoration as TerminatorImpl).markerUrl
            }
        } else if ("markerend" == name) {
            if (toDecoration is TerminatorImpl) {
                return (toDecoration as TerminatorImpl).markerUrl
            }
        }
        return null
    }

    override fun getDecorationForEnd(end: DiagramElement): Terminator {
        ensureInitialized()
        return if (from === end) {
            fromDecoration!!
        } else if (to === end) {
            toDecoration!!
        } else {
            throw LogicException("Trying to get decoration for an end that isn't from or to")
        }
    }

    override fun getMinimumLength(): Double {
        ensureInitialized()
        return minimumLength
    }

    override fun getCornerRadius(): Double {
        ensureInitialized()
        return cornerRadius
    }

    override fun getFromArrivalSide(): Direction? {
        ensureInitialized()
        return if (fromDecoration != null && fromDecoration!!.getArrivalSide() != null) {
            fromDecoration!!.getArrivalSide()
        } else reverse(drawDirection)
    }

    override fun getToArrivalSide(): Direction? {
        ensureInitialized()
        return if (toDecoration != null && toDecoration!!.getArrivalSide() != null) {
            toDecoration!!.getArrivalSide()
        } else drawDirection
    }
}