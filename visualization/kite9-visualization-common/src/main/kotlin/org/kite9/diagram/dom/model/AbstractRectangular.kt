package org.kite9.diagram.dom.model

import org.kite9.diagram.dom.bridge.ElementContext
import org.kite9.diagram.dom.css.CSSConstants
import org.kite9.diagram.dom.painter.LeafPainter
import org.kite9.diagram.dom.painter.Painter
import org.kite9.diagram.dom.processors.xpath.XPathAware
import org.kite9.diagram.dom.transform.LeafTransformer
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.*
import org.kite9.diagram.model.position.*
import org.kite9.diagram.model.position.CostedDimension2D.Companion.ZERO
import org.kite9.diagram.model.style.ContainerPosition
import org.kite9.diagram.model.style.ContentTransform
import org.kite9.diagram.model.style.DiagramElementSizing
import org.kite9.diagram.model.style.GridContainerPosition
import org.w3c.dom.Element
import kotlin.math.max

abstract class AbstractRectangular(
    el: Element,
    parent: DiagramElement?,
    ctx: ElementContext,
    rp: Painter,
    t: ContentTransform
) : AbstractModelDiagramElement(
    el, parent, ctx, rp, t
), Rectangular, XPathAware {

    private var ri: RectangleRenderingInformation = RectangleRenderingInformationImpl()
    private var layout: Layout? = null
    protected var sizingHoriz: DiagramElementSizing = DiagramElementSizing.MINIMIZE
    protected var sizingVert: DiagramElementSizing = DiagramElementSizing.MINIMIZE

    override fun getRenderingInformation(): RectangleRenderingInformation {
        return ri
    }

    override fun initialize() {
        super.initialize()
        initContainerPosition()
        initSizing()
        initLayout()
    }

    fun getLayout(): Layout? {
        ensureInitialized()
        return layout
    }

    protected fun initLayout() {
        layout = ctx.getCSSStyleEnumProperty(CSSConstants.LAYOUT_PROPERTY, theElement, Layout::class)
    }

    protected fun initSizing() {
        sizingHoriz =
            ctx.getCSSStyleEnumProperty(CSSConstants.ELEMENT_HORIZONTAL_SIZING_PROPERTY, theElement, DiagramElementSizing::class)!!
        sizingVert =
            ctx.getCSSStyleEnumProperty(CSSConstants.ELEMENT_VERTICAL_SIZING_PROPERTY, theElement, DiagramElementSizing::class)!!
    }

    protected fun initContainerPosition() {
        if (containerPosition == null) {
            if (getParent() is Container) {
                if (getContainer()!!.getLayout() === Layout.GRID) {
                    val x = ctx.getCSSStyleRangeProperty(CSSConstants.GRID_OCCUPIES_X_PROPERTY, theElement)
                    val y = ctx.getCSSStyleRangeProperty(CSSConstants.GRID_OCCUPIES_Y_PROPERTY, theElement)
                    containerPosition = GridContainerPosition(x!!, y!!)
                }
            }
            if (containerPosition == null) {
                containerPosition = NO_CONTAINER_POSITION
            }
        }
    }

    private var containerPosition: ContainerPosition? = null
    override fun getContainerPosition(): ContainerPosition? {
        ensureInitialized()
        return containerPosition
    }

    override fun getContainer(): Container? {
        return getParent() as Container?
    }

    override fun getXPathVariable(name: String): String? {
        if ("x0" == name || "y0" == name) {
            return "0"
        } else if ("y1" == name || "height" == name) {
            return "" + getRenderingInformation().size!!.h
        } else if ("x1" == name || "width" == name) {
            return "" + getRenderingInformation().size!!.w
        } else if (getLayout() === Layout.GRID && this is Container) {
            val cellX = name.startsWith("cell-x-")
            val cellY = name.startsWith("cell-y-")
            if (cellX) {
                val idx = safeParseInt(name)
                return if (idx > -1) "" + getRenderingInformation().cellXPositions!![idx] else null
            } else if (cellY) {
                val idx = safeParseInt(name)
                return if (idx > -1) "" + getRenderingInformation().cellYPositions!![idx] else null
            }
        }
        return null
    }

    protected fun safeParseInt(name: String): Int {
        return try {
            name.substring(7).toInt()
        } catch (e: Exception) {
            -1
        }
    }


    companion object {
        val NO_CONTAINER_POSITION: ContainerPosition = object : ContainerPosition {
            override fun toString(): String {
                return "none"
            }
        }
    }
}