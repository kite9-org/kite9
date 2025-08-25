package org.kite9.diagram.dom.transform

import org.kite9.diagram.dom.painter.LeafPainter
import org.kite9.diagram.dom.painter.Painter
import org.kite9.diagram.dom.processors.XMLProcessor
import org.kite9.diagram.model.Leaf
import org.kite9.diagram.model.SizedRectangular
import org.kite9.diagram.model.position.BasicDimension2D
import org.kite9.diagram.model.position.CostedDimension2D.Companion.ZERO
import org.kite9.diagram.model.position.Dimension2D
import org.kite9.diagram.model.position.Direction
import org.w3c.dom.Document
import org.w3c.dom.Element

/**
 * This makes sure the content is positioned correctly inside it's container.
 *
 * Content is all cropped between top-left and bottom right positions.
 */
open class CroppingTransformer(private val owner: Leaf) :
        AbstractRectangularTransformer(), LeafTransformer {

    override fun postProcess(p: Painter, d: Document, postProcessor: XMLProcessor): Element? {
        // work out translation
        var position = getRenderedRelativePosition(owner)
        val out = p.output(d, postProcessor)
        if (p is LeafPainter && out != null) {
            val content = p.bounds()
            if (content != null) {
                position = position.minus(BasicDimension2D(content.x, content.y))
                if (owner is SizedRectangular) {
                    val left = (owner as SizedRectangular).getPadding(Direction.LEFT)
                    val top = (owner as SizedRectangular).getPadding(Direction.UP)
                    position = position.add(BasicDimension2D(left, top))
                }
                if (position.x() != 0.0 || position.y() != 0.0) {
                    out.setAttribute(
                        "transform",
                        "translate(" +
                                oneDecimal(position.x()) +
                                "," +
                                oneDecimal(position.y()) +
                                ")"
                    )
                }
            }
        }
        return out
    }

    override fun getBounds(p: LeafPainter): Dimension2D {
        val r = p.bounds() ?: return ZERO
        return BasicDimension2D(r.width, r.height)
    }
}
