package org.kite9.diagram.dom.transform

import org.kite9.diagram.dom.painter.Painter
import org.kite9.diagram.dom.processors.XMLProcessor
import org.w3c.dom.Document
import org.w3c.dom.Element

/**
 * This class handles the sizing transformations added to the SVG <g> elements we
 * create in the Painter.  Depending on the sizing approach, we might be transforming the content to be
 * the same size as the container, or in some way based on the container.
 *
 * @author robmoffat
</g> */
interface SVGTransformer {

    fun postProcess(p: Painter, d: Document, postProcessor: XMLProcessor): Element?

}