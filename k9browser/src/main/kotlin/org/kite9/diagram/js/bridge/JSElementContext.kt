package org.kite9.diagram.js.bridge

import org.kite9.diagram.common.range.IntegerRange
import org.kite9.diagram.dom.bridge.ElementContext
import org.kite9.diagram.logging.Kite9ProcessingException
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.style.ConnectionAlignment
import org.w3c.dom.Element
import kotlinx.browser.document

class JSElementContext : ElementContext {

    override fun getCssStyleDoubleProperty(prop: String, e: Element): Double {
        val s = e.getComputedStyleMap().get(prop);
        return s as Double
    }

    override fun getCssStyleStringProperty(prop: String, e: Element): String? {
        TODO("Not yet implemented")
    }

    override fun getCSSStyleEnumProperty(prop: String, e: Element): Any? {
        TODO("Not yet implemented")
    }

    override fun getCSSStyleRangeProperty(prop: String, e: Element): IntegerRange? {
        TODO("Not yet implemented")
    }

    override fun getChildDiagramElements(theElement: Element, parent: DiagramElement): List<DiagramElement> {
        TODO("Not yet implemented")
    }

    override fun getConnectionAlignment(prop: String, e: Element): ConnectionAlignment {
        TODO("Not yet implemented")
    }

    override fun getReferencedElement(prop: String, e: Element): DiagramElement? {
        TODO("Not yet implemented")
    }

    override fun getReference(prop: String, e: Element): String? {
        TODO("Not yet implemented")
    }

    override fun contextualException(reason: String, t: Throwable, e: Element): Kite9ProcessingException {
        return Kite9ProcessingException(reason+" "+t.message+" on "+e);
    }

    override fun contextualException(reason: String, e: Element): Kite9ProcessingException {
        return Kite9ProcessingException("$reason on $e");
    }

}