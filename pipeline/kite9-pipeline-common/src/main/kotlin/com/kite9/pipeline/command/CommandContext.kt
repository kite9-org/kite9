package com.kite9.pipeline.command

import com.kite9.k9server.pipeline.adl.holder.pipeline.ADLDom
import org.kite9.diagram.common.range.IntegerRange
import org.w3c.dom.Attr
import org.w3c.dom.Document
import org.w3c.dom.Element

interface CommandContext {

    fun log(message: String)

    /**
     * Creates a unique ID to be used in the document
     */
    fun uniqueId(d: Document) : String

    fun setOwnerElement(child: Attr, parent: Element);

    fun twoElementsAreIdentical(expected: Element?, actual: Element?): Command.Mismatch?

    fun decodeElement(base64: String?, adl: ADLDom): Element

    fun getStyleValue(e: Element, name: String): String?

    fun getStyleRangeValue(el: Element, name: String): IntegerRange?

    /**
     * If value is null, removes the attribute.
     */
    fun setStyleValue(e: Element, name: String, value: String?)

    /**
     * A way of setting attributes from a given element, using an xpath to define the attribute.
     */
    fun setAttributeValue(insert: Element, xpath: String, value: String)
}