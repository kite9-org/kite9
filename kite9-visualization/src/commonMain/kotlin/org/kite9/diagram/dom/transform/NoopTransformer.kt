package org.kite9.diagram.dom.transform

import org.kite9.diagram.dom.painter.Painter
import org.kite9.diagram.dom.processors.XMLProcessor
import org.w3c.dom.Document
import org.w3c.dom.Element

class NoopTransformer : SVGTransformer {

    override fun postProcess(p: Painter, d: Document, postProcessor: XMLProcessor): Element? {
        return p.output(d, postProcessor)
    }
}