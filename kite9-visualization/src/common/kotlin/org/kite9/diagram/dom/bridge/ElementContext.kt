package org.kite9.diagram.dom.bridge

import org.kite9.diagram.common.range.IntegerRange
import org.kite9.diagram.dom.processors.xpath.XPathAware
import org.kite9.diagram.logging.Kite9ProcessingException
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.Rectangle2D
import org.kite9.diagram.model.style.HorizontalAlignment
import org.kite9.diagram.model.style.Placement
import org.kite9.diagram.model.style.VerticalAlignment
import org.w3c.dom.Element
import kotlin.reflect.KClass
interface ElementContext {

    companion object {
        val UNITS: Set<String> = setOf("pt", "cm", "em", "in", "ex", "px")

        inline fun <reified  X : Enum<X>> getCssStyleEnumProperty(prop: String, e: Element, ec: ElementContext): X? {
            val s = ec.getCssStyleStringProperty(prop, e)
            if (s==null) {
                return null
            } else {
                return enumValues<X>()
                    .firstOrNull { it.name.toLowerCase().replace("_", "-") == s.trim() }
            }
        }
    }

    /**
     * For returning a pixel amount
     */
    fun getCssStyleDoubleProperty(prop: String, e: Element): Double
    fun getCssStyleStringProperty(prop: String, e: Element): String?
    fun getCssStyleRangeProperty(prop: String, e: Element): IntegerRange?
    fun getCssStylePlacementProperty(prop: String, e: Element): Placement

    fun addChild(parent: DiagramElement, out: DiagramElement)
    fun getChildDiagramElements(parent: DiagramElement): MutableList<DiagramElement>

    // these might get removed now
    fun getReferencedElement(id: String, e: Element): DiagramElement?
    fun getReference(prop: String, e: Element): String?

    fun evaluateXPath(x: String, e: Element) : String?

    fun contextualException(reason: String, t: Throwable, e: Element): Kite9ProcessingException
    fun contextualException(reason: String, e: Element): Kite9ProcessingException

    fun register(x: Element, out: DiagramElement)
    fun getRegisteredDiagramElement(x: Element): DiagramElement?
    fun getDocumentReplacer(at: Element): XPathAware

    fun bounds(x: Element) : Rectangle2D?

    /**
     * Returns length of text, used for wrapping.
     */
    fun textWidth(s: String, inside: Element) : Double

    fun getCssUnitSizeInPixels(prop: String, e: Element) : Double

}

