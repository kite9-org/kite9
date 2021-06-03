package com.kite9.k9server.adl.holder.pipeline

import com.kite9.k9server.adl.holder.meta.MetaReadWrite
import com.kite9.k9server.uri.URI
import org.w3c.dom.Document

/**
 * Second point in the pipeline, the DOM can be worked on by commands.
 *
 * @author robmoffat
 */
interface XMLDom : XMLBase, MetaReadWrite {

    /**
     * Returns this document
     */
    val document: Document

    /**
     * For parsing a referenced document, provided in content.
     */
    fun parseDocument(content: String?, uri: URI?): Document?

    /**
     * For loading up a referenced document.
     */
    fun parseDocument(uri: URI?): Document?
}