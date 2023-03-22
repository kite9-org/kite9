package org.kite9.diagram.dom.processors

import org.kite9.diagram.dom.bridge.ElementContext
import org.kite9.diagram.dom.model.HasSVGRepresentation
import org.kite9.diagram.dom.ns.Kite9Namespaces
import org.kite9.diagram.dom.processors.xpath.ValueReplacer
import org.w3c.dom.Attr
import org.w3c.dom.Element

open class DiagramPositionProcessor(val ctx : ElementContext, val vr: ValueReplacer) : AbstractInlineProcessor() {


    private val noop = NullXMLProcessor()

    override fun processTag(n: Element): Element {
        val de = ctx.getRegisteredDiagramElement(n)

        if (de is HasSVGRepresentation) {
            de.output(n.ownerDocument!!, noop)
        } else {
            super.processTag(n)
        }

        processAttributes(n, n)
        return n
    }

    open fun processAttributes(n: Element, context: Element?) {
        for (j in 0 until n.attributes.length) {
            val a = n.attributes.item(j) as Attr?
            if (a != null) {
                if (canValueReplace(a, n)) {
                    val oldValue: String = a.value
                    val newValue: String = vr.performValueReplace(oldValue, n)
                    if (oldValue != newValue) {
                        updateAttribute(n, a, newValue)
                    }
                }
            }
        }
    }

    protected fun updateAttribute(n: Element, a: Attr, newValue: String) {
        val attName = a.localName;
        if (newValue.isEmpty()) {
            n.removeAttribute(attName)
        } else {
            n.setAttribute(attName, newValue)
        }
    }

    protected fun canValueReplace(n: Attr, e: Element): Boolean {
        return n.namespaceURI == Kite9Namespaces.POSTPROCESSOR_NAMESPACE
    }


}