package org.kite9.diagram.dom.painter

import org.kite9.diagram.dom.processors.XMLProcessor
import org.w3c.dom.Document
import org.w3c.dom.Element

/**
 * Base class for painter implementations where we are simply copying some XML from the
 * source to the destination.
 */
open class DirectSVGGroupPainter(protected var theElement: Element) : AbstractPainter() {
    /**
     * The basic output approach is to turn any DiagramElement into a <g> tag, with the same ID set
     * as the DiagramElement.
    </g> */
    override fun output(d: Document, postProcessor: XMLProcessor): Element? {
        val out = processOutput(theElement, d, postProcessor)
        out?.let { addInfoAttributes(theElement) }
        return out
    }

    protected open fun processOutput(`in`: Element?, d: Document?, postProcessor: XMLProcessor): Element? {
        val out = postProcessor.processContents(`in`) as Element?
        handleTemporaryElements(out!!, d!!, postProcessor)
        return out
    }

}