package org.kite9.diagram.dom.painter

import org.apache.batik.util.SVGConstants
import org.kite9.diagram.dom.processors.XMLProcessor
import org.kite9.diagram.model.position.RectangleRenderingInformation
import org.w3c.dom.Document
import org.w3c.dom.Element

class SVGRectPainter(private val classes: String) : AbstractPainter() {

    override fun output(d: Document, postProcessor: XMLProcessor): Element? {
        val rri = r!!.getRenderingInformation() as RectangleRenderingInformation
        val size = rri.size
        return if (size!!.w > 0 && size.h > 0) {
            val out = d.createElementNS("http://www.w3.org/2000/svg", "rect")
            out.setAttribute("width", "" + size.w + "px")
            out.setAttribute("height", "" + size.h + "px")
            out.setAttribute("class", classes)
            val parent = r!!.getParent()
            val position = rri.position
            val parentPosition = (parent!!.getRenderingInformation() as RectangleRenderingInformation).position
            val offsetPosition = position!!.minus(parentPosition!!)
            out.setAttribute("x", "" + offsetPosition.w + "px")
            out.setAttribute("y", "" + offsetPosition.h + "px")
            addInfoAttributes(out)
            out
        } else {
            null
        }
    }
}