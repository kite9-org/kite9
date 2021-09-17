package org.kite9.diagram.format

import org.w3c.dom.Document

/**
 * Common interface for converting from an input DOM document (usually plain XML with a stylesheet)
 * into some output format.
 */
interface Kite9Transcoder {

    fun addTranscodingHint(key: Any, value: Any)

    fun transcode(doc: Document): Document
}