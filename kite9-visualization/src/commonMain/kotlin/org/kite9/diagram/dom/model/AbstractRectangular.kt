package org.kite9.diagram.dom.model

import org.kite9.diagram.dom.bridge.ElementContext
import org.kite9.diagram.dom.css.CSSConstants
import org.kite9.diagram.dom.painter.LeafPainter
import org.kite9.diagram.dom.painter.Painter
import org.kite9.diagram.dom.processors.xpath.XPathAware
import org.kite9.diagram.model.*
import org.kite9.diagram.model.position.*
import org.kite9.diagram.model.style.ContainerPosition
import org.kite9.diagram.model.style.ContentTransform
import org.kite9.diagram.model.style.DiagramElementSizing
import org.kite9.diagram.model.style.GridContainerPosition
import org.w3c.dom.Element

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
        layout = ElementContext.getCssStyleEnumProperty<Layout>(CSSConstants.LAYOUT_PROPERTY, theElement, ctx)
    }

    protected fun initSizing() {
        sizingHoriz =
            ElementContext.getCssStyleEnumProperty<DiagramElementSizing>(CSSConstants.ELEMENT_HORIZONTAL_SIZING_PROPERTY, theElement, ctx)!!
        sizingVert =
            ElementContext.getCssStyleEnumProperty<DiagramElementSizing>(CSSConstants.ELEMENT_VERTICAL_SIZING_PROPERTY, theElement, ctx)!!
    }

    protected fun initContainerPosition() {
        if (containerPosition == null) {
            if (getParent() is Container) {
                if (getContainer()!!.getLayout() === Layout.GRID) {
                    val x = ctx.getCssStyleRangeProperty(CSSConstants.GRID_OCCUPIES_X_PROPERTY, theElement)
                    val y = ctx.getCssStyleRangeProperty(CSSConstants.GRID_OCCUPIES_Y_PROPERTY, theElement)
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
        if ("x0" == name || "x" == name) {
            if (painter is LeafPainter) {
                return "" + (painter as LeafPainter).bounds().x;
            } else {
                return "0";
            }
        } else if ("y0" == name || "y" == name) {
            if (painter is LeafPainter) {
                return "" + (painter as LeafPainter).bounds().y;
            } else {
                return "0";
            }
        } else if ("y1" == name || "height" == name) {
            return "" + (getRenderingInformation().size?.h ?: "0")
        } else if ("x1" == name || "width" == name) {
            return "" + (getRenderingInformation().size?.w ?: "0")
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


    override fun deepContains(d: DiagramElement): Boolean {
        return if ((d.getDepth() > this.getDepth()) && (this is Container)) {
            getContents()
                .filterIsInstance<Rectangular>().
                firstOrNull { it == d || it.deepContains(d) } != null
        } else {
            false
        }
    }
}