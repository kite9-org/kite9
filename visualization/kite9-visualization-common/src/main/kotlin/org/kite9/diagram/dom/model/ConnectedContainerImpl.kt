package org.kite9.diagram.dom.model

import org.kite9.diagram.dom.bridge.ElementContext
import org.kite9.diagram.dom.css.CSSConstants
import org.kite9.diagram.dom.painter.Painter
import org.kite9.diagram.model.Connection
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.model.style.BorderTraversal
import org.kite9.diagram.model.style.ContentTransform
import org.w3c.dom.Element

open class ConnectedContainerImpl(
    el: Element,
    parent: DiagramElement?,
    ctx: ElementContext,
    rp: Painter,
    t: ContentTransform
) : AbstractConnectedRectangular(
    el, parent, ctx, rp, t
), Container {

    companion object {
        val TRAVERSAL_PROPERTIES: MutableMap<Direction, String> = HashMap()

        init {
            TRAVERSAL_PROPERTIES[Direction.UP] =
                CSSConstants.TRAVERSAL_TOP_PROPERTY
            TRAVERSAL_PROPERTIES[Direction.DOWN] =
                CSSConstants.TRAVERSAL_BOTTOM_PROPERTY
            TRAVERSAL_PROPERTIES[Direction.LEFT] =
                CSSConstants.TRAVERSAL_LEFT_PROPERTY
            TRAVERSAL_PROPERTIES[Direction.RIGHT] =
                CSSConstants.TRAVERSAL_RIGHT_PROPERTY
        }
    }

    override fun initialize() {
        super.initialize()
        initLayout()
        initSizing()
        initConnections()
    }

    override fun getTraversalRule(d: Direction): BorderTraversal {
        return ctx.getCSSStyleEnumProperty(TRAVERSAL_PROPERTIES[d]!!, theElement, BorderTraversal::class)!!
    }

    override fun getGridColumns(): Int {
        return if (getLayout() === Layout.GRID) {
            ctx.getCssStyleDoubleProperty(CSSConstants.GRID_COLUMNS_PROPERTY, theElement).toInt()
        } else {
            0
        }
    }

    override fun getGridRows(): Int {
        return if (getLayout() === Layout.GRID) {
            ctx.getCssStyleDoubleProperty(CSSConstants.GRID_ROWS_PROPERTY, theElement).toInt()
        } else {
            0
        }
    }

    fun initConnections() {
        ctx.getChildDiagramElements(this)
            .filterIsInstance<Connection>()
            .forEach { registerConnection(it) }
    }

    override fun getContents(): MutableList<DiagramElement> {
        ensureInitialized()
        return ctx.getChildDiagramElements(this)
    }
}