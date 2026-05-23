package org.kite9.diagram.dom.transform

import org.kite9.diagram.dom.painter.LeafPainter
import org.kite9.diagram.dom.painter.Painter
import org.kite9.diagram.dom.processors.XMLProcessor
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.BasicDimension2D
import org.kite9.diagram.model.position.Dimension2D
import org.w3c.dom.Document
import org.w3c.dom.Element


/**
 * This makes sure the content is positioned correctly inside it's container.
 *
 * Content is expected to be defined from 0,0
 *
 */
class PositioningTransformer(private val owner: DiagramElement) : AbstractRectangularTransformer(), LeafTransformer {

    override fun postProcess(p: Painter, d: Document, postProcessor: XMLProcessor): Element? {
        // work out translation
        val position = getRenderedRelativePosition(owner)
        val out = p.output(d, postProcessor)
        if ((position.x() != 0.0 || position.y() != 0.0) && out != null) {
            out.setAttribute(
                "transform",
                "translate(" + oneDecimal(position.x()) + "," + oneDecimal(position.y()) + ")"
            )
        }
        return out
    }

    override fun getBounds(p: LeafPainter): Dimension2D {
        val r = p.bounds()
        return BasicDimension2D(r.maxX, r.maxY)
    }
}