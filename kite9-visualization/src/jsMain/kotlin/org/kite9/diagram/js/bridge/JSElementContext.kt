package org.kite9.diagram.js.bridge

import CSSRegistry
import kotlinx.browser.window
import org.kite9.diagram.common.range.BasicIntegerRange
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
import org.w3c.dom.svg.*
import kotlin.random.Random


class JSElementContext : ElementContext {

    private val children = mutableMapOf<DiagramElement, MutableList<DiagramElement>>()

    val css = window.asDynamic().CSS as CSSRegistry


    /**
     * Returns the generic (i.e. non-directed) css property, if there is one
     */
    private fun getUndirectedVersion(prop: String) : Pair<String?, Int> {
        if (prop.endsWith(CSSConstants.TOP)) {
            return Pair(prop.substring(0, prop.length - 3 - 1),0)
        }
        if (prop.endsWith(CSSConstants.RIGHT)) {
            return Pair(prop.substring(0, prop.length - 5 - 1),1)
        }
        if (prop.endsWith(CSSConstants.BOTTOM)) {
            return Pair(prop.substring(0, prop.length - 6 - 1),2)
        }
        if (prop.endsWith(CSSConstants.LEFT)) {
            return Pair(prop.substring(0, prop.length - 4 - 1),3)
        }
        if (prop.endsWith(CSSConstants.WIDTH)) {
            return Pair(prop.substring(0, prop.length - 5) + CSSConstants.SIZE, 0)
        }
        if (prop.endsWith(CSSConstants.HEIGHT)) {
            return Pair(prop.substring(0, prop.length -6) + CSSConstants.SIZE,1)
        }

        return Pair(null, 0);
    }

    override fun addChild(parent: DiagramElement, out: DiagramElement) {
        val contents = children.getOrPut(parent) { mutableListOf<DiagramElement>() }
        contents.add(out)
    }

    private val xmlToDiagram = mutableMapOf<String, DiagramElement>()

    private val charset = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    /**
     * This uses an attribute on the element called data-id to store
     * a key for the hash map.
     */
    override fun register(x: Element, out: DiagramElement) {
        var id: String?  = x.getAttribute("data-id")
        if (id == null) {
            id = (1..6)
                .map { i -> charset.get(Random.nextInt(charset.size)) }
                .joinToString("");
            x.setAttribute("data-id",id)
        }
        xmlToDiagram[id] = out
    }

    override fun getRegisteredDiagramElement(x: Element) : DiagramElement? {
        var id: String?  = x.getAttribute("data-id")
        if (id != null) {
            return xmlToDiagram[id]
        } else {
            return null;
        }
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
        var v  = (e.asDynamic().computedStyleMap() as StylePropertyMapReadOnly).get(prop)

        if ((v == null) || (v.value == "none")) {
            // try the generic property
            val (size, idx) = getUndirectedVersion(prop)
            if (size != null) {
                val sizeVals = (e.asDynamic().computedStyleMap() as StylePropertyMapReadOnly).getAll(size)
                if (sizeVals != null) {
                    v= when (sizeVals.size) {
                        0 -> return 0.0;
                        1 -> sizeVals[0]
                        2 -> sizeVals[idx % 2]
                        else -> sizeVals[idx]
                    }
                }
            }
        }

        return if ((v != null) && (v.unit != null)) {
            // convert to pixels
            val vPx = v.to("px")
            vPx.value as Double
        } else {
            0.0
        }
    }

    override fun getCssStyleStringProperty(prop: String, e: Element): String? {
        val s = (e.asDynamic().computedStyleMap() as StylePropertyMapReadOnly).get(prop)
        if (s == null) {
            return null
        }

        var out = if (s.length !== undefined) {
            s.asDynamic()[0] as String
        } else {
            s.value as String
        }

        val trimmed = out.trim()
        if ((trimmed.startsWith("\"")) && (trimmed.endsWith("\""))) {
            out = trimmed.substring(1, trimmed.length - 1)
        }

        if (out == "none") {
            // try the generic property
            val ( generic, idx)  = getUndirectedVersion(prop)
            if (generic != null) {
                return getCssStyleStringProperty(generic, e)
            }
        }

        return out
    }

//    override fun <X : Enum<Any>> getCssStyleEnumProperty(prop: String, e: Element, c: KClass<VerticalAlignment>): X? {
//        val s = getCssStyleStringProperty(prop, e)
//        if (s==null) {
//            return null
//        } else {
//            // find the matching enum
//            Class.forNa
//            c.enum
//            return (c.asDynamic().jClass.values() as Array<X>)
//                .firstOrNull { (it.asDynamic().name as String).toLowerCase().replace("_", "-") == s.trim() }
//        }
//    }

    override fun getCssStyleRangeProperty(prop: String, e: Element): IntegerRange? {
        val props = (e.asDynamic().computedStyleMap() as StylePropertyMapReadOnly).getAll(prop)

        return when (props.size ?: 0) {
            0 -> null
            1 -> BasicIntegerRange(props[0].value as Int, props[0].value as Int)
            else -> BasicIntegerRange(props[0].value as Int, props[1].value as Int)
        }
    }

    override fun getChildDiagramElements(parent: DiagramElement): MutableList<DiagramElement> {
        return children.getOrElse(parent) { mutableListOf() }
    }

    override fun getCssStylePlacementProperty(prop: String, e: Element): Placement {
        val s = (e.asDynamic().computedStyleMap() as StylePropertyMapReadOnly).get(prop)

        if (s.asDynamic().unit == "percent") {
            return Placement(Measurement.PERCENTAGE, s.asDynamic().value);
        } else if (s.asDynamic().unit == "pixels") {
            return Placement(Measurement.PIXELS, s.asDynamic().value);
        } else {
            return NONE;
        }

    }

    fun ownerDocument(e: Element?) : SVGSVGElement? {
        var ee = e;
        while ((ee !is SVGSVGElement) && (ee != null)) {
            ee = ee.parentElement
        }

        return ee as SVGSVGElement?
    }

    override fun getReferencedElement(id: String, e: Element): DiagramElement? {
        val ownerDocument = ownerDocument(e)!!
        val out = ownerDocument.getElementById(id)
        return out?.let { getRegisteredDiagramElement(it) }
    }

    override fun getReference(prop: String, e: Element): String? {
        var xpath = getCssStyleStringProperty(prop, e)
        return evaluateXPath(xpath!!, e)
    }

    override fun evaluateXPath(xpath: String, e: Element): String? {
        val ownerDocument = e.ownerDocument!!
        var ns = ownerDocument.asDynamic().createNSResolver (e)
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
        val bbox = g.getBBox()!!;

        return Rectangle2D(
            bbox.x,
            bbox.y,
            bbox.width,
            bbox.height)
    }

    override fun textWidth(sIn: String, inside: Element): Double {
        // rendered content of the element
        val originalContent = inside?.textContent  ?: ""
        val normalizedContent = originalContent
            .replace(Regex("\\s+"), " ")
            .replace(Regex("\\n"), "")
            .trim()

        // rendered content of the string
        val sRendered = sIn
            .replace(Regex("\\s+"), " ")
            .replace(Regex("\\n"), "")

        if (sRendered.isEmpty()) {
            return 0.0;
        }


        val firstChar = normalizedContent.indexOf(sRendered) ?: -1
        if (firstChar == -1) {
            throw Kite9ProcessingException("Trying to find substring $sRendered inside $normalizedContent")
        }

        console.log("Looking for $sRendered inside $normalizedContent firstChar $firstChar");

        val lastChar = firstChar + sRendered.length - 1

        return if (inside is SVGTextContentElement) {
            if (lastChar >= inside.getNumberOfChars()) {
                throw Kite9ProcessingException("Character Count Too Long")
            }

            var charTL = inside.getStartPositionOfChar(firstChar)
            var charBR = inside.getEndPositionOfChar(lastChar)
            charBR.x - charTL.x
        } else {
            0.0
        }
    }

    override fun getCssUnitSizeInPixels(prop: String, e: Element): Double {
        return when (prop) {
            "cm" -> css.cm("1").to("px").asDynamic().value
            "mm" -> css.mm("1").to("px").asDynamic().value
            "pt" -> css.pt("1").to("px").asDynamic().value
            "pc" -> css.pc("1").to("px").asDynamic().value
            "em" -> css.em("1").to("px").asDynamic().value
            else -> 1.0;
        } as Double
    }
}