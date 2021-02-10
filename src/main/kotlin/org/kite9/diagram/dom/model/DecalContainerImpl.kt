package org.kite9.diagram.dom.model

import org.kite9.diagram.dom.bridge.ElementContext
import org.kite9.diagram.dom.css.CSSConstants
import org.kite9.diagram.dom.painter.Painter
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.Decal
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.model.position.RectangleRenderingInformation
import org.kite9.diagram.model.style.BorderTraversal
import org.kite9.diagram.model.style.ContentTransform
import org.w3c.dom.Element

class DecalContainerImpl(
    el: Element,
    parent: DiagramElement,
    context: ElementContext,
    p: Painter,
    t: ContentTransform
) : AbstractRectangular(
    el, parent, context, p, t
), Decal, Container {

    private var contents: MutableList<DiagramElement>? = null
    override fun getRenderingInformation(): RectangleRenderingInformation {
        return getParent()!!.getRenderingInformation() as RectangleRenderingInformation
    }

    override fun getContents(): MutableList<DiagramElement> {
        ensureInitialized()
        return contents!!
    }

    override fun getTraversalRule(d: Direction): BorderTraversal {
        return BorderTraversal.NONE
    }

    override fun getGridColumns(): Int {
        return if (getLayout() === Layout.GRID) {
            ctx.getCssDoubleValue(CSSConstants.GRID_COLUMNS_PROPERTY, theElement).toInt()
        } else {
            0
        }
    }

    override fun getGridRows(): Int {
        return if (getLayout() === Layout.GRID) {
            ctx.getCssDoubleValue(CSSConstants.GRID_ROWS_PROPERTY, theElement).toInt()
        } else {
            0
        }
    }

    override fun initialize() {
        super.initialize()
        initLayout()
        initSizing()
        initContents()
    }
}