package org.kite9.diagram.js.bridge

import org.kite9.diagram.common.range.IntegerRange
import org.kite9.diagram.dom.bridge.ElementContext
import org.kite9.diagram.logging.Kite9ProcessingException
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.style.BorderTraversal
import org.kite9.diagram.model.style.ConnectionAlignment
import org.w3c.dom.Element
import kotlin.reflect.KClass

class JSElementContext : ElementContext {

    override fun getCssStyleDoubleProperty(prop: String, e: Element, ): Double {
        val s = (e.asDynamic().computedStyleMap() as StylePropertyMapReadOnly).get(prop)
        return 0.0
    }

    override fun getCssStyleStringProperty(prop: String, e: Element): String? {
        val s = (e.asDynamic().computedStyleMap() as StylePropertyMapReadOnly).get(prop)
        return if (s == null) {
            null
        } else {
            s[0].trim()
        }
    }

    override fun <X : Any> getCSSStyleEnumProperty(prop: String, e: Element, c: KClass<X>): X {
        val s = getCssStyleStringProperty(prop, e)!!.trim()
        return (c.asDynamic().jClass.values() as Array<X>)
            .filter { (it.asDynamic().name as String).toLowerCase() == s }
            .first()
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