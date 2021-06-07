package com.kite9.pipeline.adl.holder.pipeline

import com.kite9.pipeline.adl.holder.meta.MetaReadWrite
import com.kite9.pipeline.uri.K9URI
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
    fun parseDocument(content: String, uri: K9URI): Document

    /**
     * For loading up a referenced document.
     */
    fun parseDocument(uri: K9URI): Document?
}