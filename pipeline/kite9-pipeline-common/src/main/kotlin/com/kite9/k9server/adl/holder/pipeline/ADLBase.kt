package com.kite9.k9server.adl.holder.pipeline

/**
 * This is the first part of an ADL pipeline.  At this point, it's simply a
 * request for an ADL document, with a given URL and headers, and possibly some content loaded.
 *
 * @author robmoffat
 */
interface ADLBase : XMLBase {
    /**
     * Parses the contents and returns the ADL in DOM format.
     */
    fun parse(): ADLDom
}