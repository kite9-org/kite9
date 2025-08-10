package org.kite9.diagram.dom.transform

import org.kite9.diagram.dom.painter.LeafPainter
import org.kite9.diagram.dom.painter.Painter
import org.kite9.diagram.dom.processors.XMLProcessor
import org.kite9.diagram.model.Leaf
import org.kite9.diagram.model.position.CostedDimension2D.Companion.ZERO
import org.kite9.diagram.model.position.Dimension2D
import org.w3c.dom.Document
import org.w3c.dom.Element

class RescalingTransformer(private val l: Leaf) :
        AbstractRectangularTransformer(), LeafTransformer {

    /** Ensures the decal is the same size as it's parent (for scaled decals) */
    override fun postProcess(p: Painter, d: Document, postProcessor: XMLProcessor): Element? {
        val size = getRectangularRenderedSize(l)
        val out = p.output(d, postProcessor)
        @Suppress("SENSELESS_COMPARISON")
        if (size == null) {
            // not a rectangular transform.
            return out
        }
        val width = size.width()
        val height = size.height()
        if (p is LeafPainter && out != null) {
            val myBounds = p.bounds()
            val xs = width / myBounds.width
            val ys = height / myBounds.height
            out.setAttribute(
                    "transform",
                    "scale(" +
                            xs +
                            "," +
                            ys +
                            ")" +
                            "translate(" +
                            oneDecimal(-myBounds.x) +
                            "," +
                            oneDecimal(-myBounds.y) +
                            ")"
            )
        }
        return out
    }

    override fun getBounds(p: LeafPainter): Dimension2D {
        return ZERO
    }
}
