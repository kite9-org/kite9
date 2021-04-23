package org.kite9.diagram.dom.bridge

import org.kite9.diagram.common.range.IntegerRange
import org.kite9.diagram.logging.Kite9ProcessingException
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.Rectangle2D
import org.kite9.diagram.model.style.Placement
import org.w3c.dom.Element
import kotlin.reflect.KClass

interface ElementContext {

    /**
     * For returning a pixel amount
     */
    fun getCssStyleDoubleProperty(prop: String, e: Element): Double
    fun getCssStyleStringProperty(prop: String, e: Element): String?
    fun <X : Any> getCSSStyleEnumProperty(prop: String, e: Element, c: KClass<X>): X?
    fun getCSSStyleRangeProperty(prop: String, e: Element): IntegerRange?
    fun getCSSStylePlacementProperty(prop: String, e: Element): Placement
    fun getConnectionAlignment(prop: String, e: Element): Placement

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

    fun bounds(x: Element) : Rectangle2D?

    /**
     * Returns length of text, used for wrapping.
     */
    fun textWidth(s: String, inside: Element) : Double

    fun getCssUnitSizeInPixels(prop: String, e: Element) : Double
}