package org.kite9.diagram.dom.bridge

import org.kite9.diagram.common.range.IntegerRange
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.style.ConnectionAlignment
import org.w3c.dom.Element

interface ElementContext {

    fun getCssDoubleValue(prop: String, e: Element): Double
    fun getCssStringValue(prop: String, e: Element): String?
    fun getCSSStyleProperty(prop: String, e: Element): Any?
    fun getChildDiagramElements(theElement: Element, parent: DiagramElement): List<DiagramElement>
    fun getCSSStyleRangeProperty(prop: String, e: Element): IntegerRange?
    fun getConnectionAlignment(prop: String, e: Element): ConnectionAlignment
    fun getReferencedElement(prop: String, e: Element): DiagramElement?
    fun getReference(prop: String, e: Element): String?
    fun contextualException(reason: String, t: Throwable, e: Element): RuntimeException
    fun contextualException(reason: String, e: Element): RuntimeException

}