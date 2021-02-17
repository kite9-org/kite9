package org.kite9.diagram.dom.model

import org.kite9.diagram.common.elements.factory.AbstractDiagramElement
import org.kite9.diagram.dom.painter.Painter
import org.kite9.diagram.dom.processors.XMLProcessor
import org.kite9.diagram.model.Diagram
import org.kite9.diagram.model.DiagramElement
import org.w3c.dom.Document
import org.w3c.dom.Element

/**
 * Encapsulates an [Element] as a [DiagramElement].
 *
 * @author robmoffat
 */
abstract class AbstractDOMDiagramElement(
    protected val theElement: Element,
    parent: DiagramElement?) :
    AbstractDiagramElement(parent), HasSVGRepresentation {

    private var initialized = false
    protected abstract fun initialize()
    protected fun ensureInitialized() {
        if (!initialized) {
            if (getParent() is AbstractDOMDiagramElement) {
                (getParent() as AbstractDOMDiagramElement).ensureInitialized()
            }
            initialized = true
            //initializeDOMElement(this.theElement);
            initialize()
        }
    }

    abstract val painter: Painter?
    override fun getID(): String {
        return theElement.getAttribute("id") ?: "noid-"+ (Companion.nextId++)
    }

    val diagram: Diagram
        get() = if (this is Diagram) {
            this
        } else {
            (getParent() as AbstractDOMDiagramElement).diagram
        }

    override fun toString(): String {
        return "[${theElement.tagName}:${getID()}]"
    }

    override fun output(d: Document, p: XMLProcessor): Element? {
        return if (getRenderingInformation().rendered) {
            ensureInitialized()
            paintElementToDocument(d, p)
        } else {
            null
        }
    }

    protected abstract fun paintElementToDocument(d: Document, postProcessor: XMLProcessor): Element

    /**
     * For elements which are not decals, this needs to be done before accessing properties
     * or children.
     */
    val dOMElement: Element
        get() {
            ensureInitialized()
            return theElement
        }

    /**
     * For elements which are containers, call this method as part of initialize.
     */
    protected abstract fun initContents(): List<DiagramElement?>?

    object Companion {

        var nextId: Int = 0;


    }
}