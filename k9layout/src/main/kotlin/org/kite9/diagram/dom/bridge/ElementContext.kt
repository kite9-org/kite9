package org.kite9.diagram.dom.bridge

import org.kite9.diagram.common.range.IntegerRange
import org.kite9.diagram.logging.Kite9ProcessingException
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.style.ConnectionAlignment
import org.w3c.dom.Element
import kotlin.reflect.KClass

interface ElementContext {

    fun getCssStyleDoubleProperty(prop: String, e: Element): Double
    fun getCssStyleStringProperty(prop: String, e: Element): String?

    fun <X : Any> getCSSStyleEnumProperty(prop: String, e: Element, c: KClass<X>): X

    fun getCSSStyleRangeProperty(prop: String, e: Element): IntegerRange?

    fun getChildDiagramElements(theElement: Element, parent: DiagramElement): List<DiagramElement>
    fun getConnectionAlignment(prop: String, e: Element): ConnectionAlignment
    fun getReferencedElement(prop: String, e: Element): DiagramElement?
    fun getReference(prop: String, e: Element): String?
    fun contextualException(reason: String, t: Throwable, e: Element): Kite9ProcessingException
    fun contextualException(reason: String, e: Element): Kite9ProcessingException

}