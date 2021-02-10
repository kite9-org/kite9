package org.kite9.diagram.dom.model

import org.kite9.diagram.dom.bridge.ElementContext
import org.kite9.diagram.dom.css.CSSConstants
import org.kite9.diagram.dom.elements.StyledKite9XMLElement
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

abstract class AbstractRectangular(
    el: StyledKite9XMLElement,
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
        layout = ctx.getCSSStyleProperty(CSSConstants.LAYOUT_PROPERTY, theElement) as Layout?
    }

    protected fun initSizing() {
        sizingHoriz =
            ctx.getCSSStyleProperty(CSSConstants.ELEMENT_HORIZONTAL_SIZING_PROPERTY, theElement) as DiagramElementSizing
        sizingVert =
            ctx.getCSSStyleProperty(CSSConstants.ELEMENT_VERTICAL_SIZING_PROPERTY, theElement) as DiagramElementSizing
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

    fun getSize(within: Dimension2D): CostedDimension2D {
        if (this is Decal) {
            throw LogicException("Shouldn't be using size for decals")
        } else if (this is Terminator) {
            throw LogicException("Shouldn't be using size for terminators")
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

    private fun ensureMinimumSize(c: Dimension2D, within: Dimension2D): CostedDimension2D {
        var min: Dimension2D = ZERO
        if (this is SizedRectangular) {
            min = (this as SizedRectangular).getMinimumSize()
        }
        return CostedDimension2D(
            Math.max(c.w, min.w),
            Math.max(c.h, min.h), within
        )
    }

    private val leafBounds: Dimension2D
        private get() {
            val p = painter
            return if (p is LeafPainter && transformer is LeafTransformer) {
                (transformer as LeafTransformer).getBounds(p)
            } else ZERO
        }

    companion object {
        val NO_CONTAINER_POSITION: ContainerPosition = object : ContainerPosition {
            override fun toString(): String {
                return "none"
            }
        }
    }
}