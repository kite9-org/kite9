package org.kite9.diagram.dom.processors

import org.kite9.diagram.dom.bridge.ElementContext
import org.kite9.diagram.dom.model.HasSVGRepresentation
import org.kite9.diagram.dom.processors.xpath.ValueReplacer
import org.w3c.dom.Attr
import org.w3c.dom.Element

open class DiagramPositionProcessor(val ctx : ElementContext, val vr: ValueReplacer) : AbstractInlineProcessor() {

    companion object {
        val KITE9_NAMESPACE = "http://www.kite9.org/schema/adl"
    }

    //val SVG_NAMESPACE = "http://www.w3.org/2000/svg"

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
            val a = n.attributes.item(j) as Attr
            if (canValueReplace(a, n)) {
                val oldValue: String = a.value
                val newValue: String = vr.performValueReplace(oldValue, n)
                if (oldValue != newValue) {
                    updateAttribute(n, a, newValue)
                }
            }
        }
    }

    protected fun updateAttribute(n: Element, a: Attr, newValue: String) {
        val attName = a.localName;
        n.setAttribute(attName, newValue)
    }

    protected fun canValueReplace(n: Attr, e: Element): Boolean {
        return n.namespaceURI == KITE9_NAMESPACE
        //return n.localName.startsWith("k9-")
    }


}