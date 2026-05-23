package org.kite9.diagram.dom.model

import org.kite9.diagram.dom.processors.XMLProcessor
import org.kite9.diagram.model.DiagramElement
import org.w3c.dom.Document
import org.w3c.dom.Element

/**
 * Means this [DiagramElement] can output XML
 * representing itself.
 *
 * @author robmoffat
 */
interface HasSVGRepresentation : DiagramElement {

    /**
     * Creates SVG elements representing this DiagramElement and anything
     * nested within it.
     * @param p
     */
    fun output(d: Document, p: XMLProcessor): Element?
}