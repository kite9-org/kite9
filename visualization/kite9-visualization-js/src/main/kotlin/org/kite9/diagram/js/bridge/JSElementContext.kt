package org.kite9.diagram.js.bridge

import org.kite9.diagram.common.range.IntegerRange
import org.kite9.diagram.dom.bridge.ElementContext
import org.kite9.diagram.dom.bridge.ElementContext.Companion.UNITS
import org.kite9.diagram.dom.css.CSSConstants
import org.kite9.diagram.dom.processors.xpath.XPathAware
import org.kite9.diagram.logging.Kite9ProcessingException
import org.kite9.diagram.model.Diagram
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.Rectangle2D
import org.kite9.diagram.model.style.Measurement
import org.kite9.diagram.model.style.Placement
import org.kite9.diagram.model.style.Placement.Companion.NONE
import org.w3c.dom.Element
import org.w3c.dom.svg.SVGGraphicsElement
import org.w3c.dom.svg.SVGTSpanElement
import org.w3c.dom.svg.SVGTextElement
import kotlin.reflect.KClass


class JSElementContext : ElementContext {

    private val children = mutableMapOf<DiagramElement, MutableList<DiagramElement>>()

    /**
     * Returns the generic (i.e. non-directed) css property, if there is one
     */
    private fun getUndirectedVersion(prop: String) : String? {
        if (prop.endsWith(CSSConstants.LEFT)) {
            return prop.substring(0, prop.length - CSSConstants.LEFT.length - 1)
        }
        if (prop.endsWith(CSSConstants.RIGHT)) {
            return prop.substring(0, prop.length - CSSConstants.RIGHT.length - 1)
        }
        if (prop.endsWith(CSSConstants.TOP)) {
            return prop.substring(0, prop.length - CSSConstants.TOP.length - 1)
        }
        if (prop.endsWith(CSSConstants.BOTTOM)) {
            return prop.substring(0, prop.length - CSSConstants.BOTTOM.length - 1)
        }

        return null;
    }


    override fun addChild(parent: DiagramElement, out: DiagramElement) {
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

    override fun getDocumentReplacer(at: Element): XPathAware {
        return object : XPathAware {
            override fun getXPathVariable(name: String): String {
                return if ("width" == name) {
                   ( xmlToDiagram.values
                        .filterIsInstance<Diagram>()
                        .maxOfOrNull{it.getRenderingInformation().position!!.x() + it.getRenderingInformation().size!!.x() }
                        ?: 0 ).toString()
                } else if ("height" == name) {
                    ( xmlToDiagram.values
                        .filterIsInstance<Diagram>()
                        .maxOfOrNull{ it.getRenderingInformation().position!!.y()!! + it.getRenderingInformation().size!!.y()!! }
                        ?: 0 ).toString()
                } else if (UNITS.contains(name)) {
                    "" + getCssUnitSizeInPixels(name, at)
                } else {
                    ""
                }
            }
        }
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
        val s = getCssStyleStringProperty(prop, e)
        if (s==null) {
            return null
        } else {
            return (c.asDynamic().jClass.values() as Array<X>)
                .firstOrNull { (it.asDynamic().name as String).toLowerCase().replace("_", "-") == s.trim() }
        }
    }

    override fun getCSSStyleRangeProperty(prop: String, e: Element): IntegerRange? {
        TODO("Not yet implemented")
    }

    override fun getCSSStylePlacementProperty(prop: String, e: Element): Placement {
        TODO("Not yet implemented")
    }

    override fun getChildDiagramElements(parent: DiagramElement): MutableList<DiagramElement> {
        return children.getOrElse(parent) { mutableListOf() }
    }

    override fun getPlacement(prop: String, e: Element): Placement {
        val s = (e.asDynamic().computedStyleMap() as StylePropertyMapReadOnly).get(prop)

        if (s.asDynamic().value == "none") {
            return NONE;
        } else if (s.asDynamic().unit == "percent") {
            return Placement(Measurement.PERCENTAGE, s.asDynamic().value);
        } else if (s.asDynamic().unit == "pixels") {
            return Placement(Measurement.PIXELS, s.asDynamic().value);
        } else {
            TODO("Not yet implemented")
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

    override fun bounds(g: Element): Rectangle2D {
        val g = g as SVGGraphicsElement
        val mtrx = g.getCTM()!!;
        val bbox = g.getBBox()!!;
        return Rectangle2D(
            (mtrx.e + bbox.x),
            (mtrx.f + bbox.y),
            bbox.width,
            bbox.height)
    }

    override fun textWidth(s: String, inside: Element): Double {
        val start = inside.textContent?.indexOf(s) ?: 0
        val end = start + s.length

        return if (inside is SVGTextElement) {
            var charTL = inside.getStartPositionOfChar(start)
            var charBR = inside.getEndPositionOfChar(end)
            charBR.x - charTL.x
        } else if (inside is SVGTSpanElement) {
            var charTL = inside.getStartPositionOfChar(start)
            var charBR = inside.getEndPositionOfChar(end)
            charBR.x - charTL.x
        } else {
            0.0
        }
    }

    override fun getCssUnitSizeInPixels(prop: String, e: Element): Double {
        TODO("Not yet implemented")
    }
}