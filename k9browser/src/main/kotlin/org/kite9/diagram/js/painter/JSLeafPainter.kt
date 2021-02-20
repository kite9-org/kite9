package org.kite9.diagram.js.painter

import org.kite9.diagram.dom.bridge.ElementContext
import org.kite9.diagram.dom.painter.DirectSVGGroupPainter
import org.kite9.diagram.dom.painter.LeafPainter
import org.kite9.diagram.model.position.Rectangle2D
import org.w3c.dom.Element
import org.w3c.dom.svg.SVGGraphicsElement

class JSLeafPainter(e: Element, val context: ElementContext) : DirectSVGGroupPainter(e), LeafPainter {

    override fun bounds(): Rectangle2D {
        val g = theElement as SVGGraphicsElement
        val mtrx = g.getCTM()!!;
        val bbox = g.getBBox()!!;
        return Rectangle2D(
            (mtrx.e + bbox.x).toDouble(),
            (mtrx.f + bbox.y).toDouble(),
            bbox.width.toDouble(),
            bbox.height.toDouble())
    }

}