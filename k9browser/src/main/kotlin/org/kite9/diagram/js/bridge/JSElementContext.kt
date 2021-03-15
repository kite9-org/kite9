package org.kite9.diagram.js.bridge

import org.kite9.diagram.common.range.IntegerRange
import org.kite9.diagram.dom.bridge.ElementContext
import org.kite9.diagram.dom.css.CSSConstants
import org.kite9.diagram.logging.Kite9ProcessingException
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.style.ConnectionAlignment
import org.kite9.diagram.model.style.ConnectionAlignment.Companion.NONE
import org.w3c.dom.Element
import kotlin.reflect.KClass

class JSElementContext : ElementContext {

    private val children = mutableMapOf<DiagramElement, MutableList<DiagramElement>>()

    /**
     * Returns the generic (i.e. non-directed) css property, if there is one
     */
    private fun getUndirectedVersion(prop: String) : String? {
        if (prop.endsWith(CSSConstants.LEFT)) {
            return prop.substring(0, prop.length - CSSConstants.LEFT.length - 1);
        }
        if (prop.endsWith(CSSConstants.RIGHT)) {
            return prop.substring(0, prop.length - CSSConstants.RIGHT.length - 1);
        }
        if (prop.endsWith(CSSConstants.TOP)) {
            return prop.substring(0, prop.length - CSSConstants.TOP.length - 1);
        }
        if (prop.endsWith(CSSConstants.BOTTOM)) {
            return prop.substring(0, prop.length - CSSConstants.BOTTOM.length - 1);
        }

        return null;
    }

    fun addChild(parent: DiagramElement, out: DiagramElement) {
        val contents = children.getOrPut(parent) { mutableListOf<DiagramElement>() }
        contents.add(out)
    }

    private val xmlToDiagram = mutableMapOf<Element, DiagramElement>()

    override fun register(x: Element, out: DiagramElement) {
        xmlToDiagram[x] = out
    }

    override fun getRegisteredDiagramElement(x: Element) : DiagramElement? {
        return xmlToDiagram[x]
    }

    override fun getCssStyleDoubleProperty(prop: String, e: Element): Double {
        val v  = (e.asDynamic().computedStyleMap() as StylePropertyMapReadOnly).get(prop)
        if (v == null) {
            return 0.0
        }

        if (v.value == "none") {
            // try the generic property
            val generic = getUndirectedVersion(prop)
            if (generic != null) {
                return getCssStyleDoubleProperty(generic, e)
            } else {
                return 0.0
            }
        }

        // convert to pixels
        val vPx = v.to("px")
        return vPx.value as Double

    }

    override fun getCssStyleStringProperty(prop: String, e: Element): String? {
        val s = (e.asDynamic().computedStyleMap() as StylePropertyMapReadOnly).get(prop)
        if (s == null) {
            return null
        }

        var out = if (s.length !== undefined) {
            s.asDynamic()[0] as String
        } else {
            s.value!! as String
        }

        if (out is String) {
            if ((out.startsWith("\"")) && (out.endsWith("\""))) {
                out = out.substring(1, out.length - 1)
            }
        }

        if (out == "none") {
            // try the generic property
            val generic = getUndirectedVersion(prop)
            if (generic != null) {
                return getCssStyleStringProperty(generic, e)
            }
        }

        return out
    }

    override fun <X : Any> getCSSStyleEnumProperty(prop: String, e: Element, c: KClass<X>): X? {
        val s = getCssStyleStringProperty(prop, e)!!.trim()
        return (c.asDynamic().jClass.values() as Array<X>)
            .firstOrNull { (it.asDynamic().name as String).toLowerCase().replace("_", "-") == s }
    }

    override fun getCSSStyleRangeProperty(prop: String, e: Element): IntegerRange? {
        TODO("Not yet implemented")
    }

    override fun getChildDiagramElements(parent: DiagramElement): List<DiagramElement> {
        return children.getOrElse(parent) { emptyList() }
    }

    override fun getConnectionAlignment(prop: String, e: Element): ConnectionAlignment {
        val s = (e.asDynamic().computedStyleMap() as StylePropertyMapReadOnly).get(prop)

        if (s.asDynamic().value == "none") {
            return NONE;
        } else {
            TODO("Not yet implemented")
//
//            if (v.getPrimitiveType() == org.w3c.dom.css.CSSPrimitiveValue.CSS_PERCENTAGE) {
//                return ConnectionAlignment(ConnectionAlignment.Measurement.PERCENTAGE, v.getFloatValue())
//            } else if (v.getPrimitiveType() == org.w3c.dom.css.CSSPrimitiveValue.CSS_PX) {
//                return ConnectionAlignment(ConnectionAlignment.Measurement.PIXELS, v.getFloatValue())
//            }
//
//            return NONE
        }

    }

    override fun getReferencedElement(id: String, e: Element): DiagramElement? {
        val ownerDocument = e.ownerDocument!!
        val out = ownerDocument.getElementById(id)
        return xmlToDiagram[out]
    }

    override fun getReference(prop: String, e: Element): String? {
        var xpath = getCssStyleStringProperty(prop, e)
        return evaluateXPath(xpath!!, e)
    }

    override fun evaluateXPath(xpath: String, e: Element): String? {
        val ownerDocument = e.ownerDocument!!
        var ns = ownerDocument.asDynamic().createNSResolver (ownerDocument.documentElement)
        val out = ownerDocument.asDynamic().evaluate(xpath, e, ns, 2) // XPathResult.STRING_TYPE)
        return out.stringValue as? String
    }

    override fun contextualException(reason: String, t: Throwable, e: Element): Kite9ProcessingException {
        return Kite9ProcessingException(reason+" "+t.message+" on "+e);
    }

    override fun contextualException(reason: String, e: Element): Kite9ProcessingException {
        return Kite9ProcessingException("$reason on $e");
    }


}