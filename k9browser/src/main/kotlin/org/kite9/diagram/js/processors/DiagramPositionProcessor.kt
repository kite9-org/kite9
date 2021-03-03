package org.kite9.diagram.js.processors

import org.kite9.diagram.dom.model.HasSVGRepresentation
import org.kite9.diagram.dom.processors.AbstractInlineProcessor
import org.kite9.diagram.dom.processors.NullXMLProcessor
import org.kite9.diagram.dom.processors.xpath.PatternValueReplacer
import org.kite9.diagram.dom.processors.xpath.ValueReplacer
import org.kite9.diagram.js.bridge.JSElementContext
import org.w3c.dom.Attr
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.Text

open class DiagramPositionProcessor(val ctx : JSElementContext, val vr: ValueReplacer) : AbstractInlineProcessor() {

    private val KITE9_NAMESPACE = "http://www.kite9.org/schema/adl"

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

    fun processAttributes(n: Element, context: Element?) {
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
        n.setAttribute(attName.substring(3), newValue)
    }

    protected fun canValueReplace(n: Attr, e: Element): Boolean {
        return n.localName.startsWith("k9-")
    }

//    override fun processText(n: Text): Text {
//        val out = super.processText(n)
//        if (canValueReplace(out)) {
//            val newData: String = vr.performValueReplace(out.getData(), n)
//            out.setData(newData)
//        }
//        return out
//    }

}