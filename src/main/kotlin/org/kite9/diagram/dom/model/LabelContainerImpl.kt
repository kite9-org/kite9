package org.kite9.diagram.dom.model

import org.kite9.diagram.dom.bridge.ElementContext
import org.kite9.diagram.dom.css.CSSConstants
import org.kite9.diagram.dom.painter.Painter
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Label
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.model.style.BorderTraversal
import org.kite9.diagram.model.style.ContentTransform
import org.w3c.dom.Element

/**
 * Container and link-end labels. (TEMPORARY)
 *
 * @author robmoffat
 */
class LabelContainerImpl(
    el: Element,
    parent: DiagramElement,
    ctx: ElementContext,
    rp: Painter,
    t: ContentTransform
) : AbstractLabel(
    el, parent, ctx, rp, t
), Label, Container {

    override fun getTraversalRule(d: Direction): BorderTraversal {
        return BorderTraversal.NONE
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

    private var contents: MutableList<DiagramElement> = mutableListOf()

    override fun initialize() {
        super.initialize()
        contents = initContents()
    }

    override fun getContents(): MutableList<DiagramElement> {
        ensureInitialized()
        return contents
    }
}