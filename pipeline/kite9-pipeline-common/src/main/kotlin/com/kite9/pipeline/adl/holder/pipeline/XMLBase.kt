package com.kite9.pipeline.adl.holder.pipeline

import com.kite9.pipeline.uri.K9URI

/**
 * Base interface that captures where an xml document came from, and it's content.
 */
interface XMLBase {

    val uri: K9URI?

    /**
     * Returns the HTTPHeaders that were responsible for loading this
     * ADL.  Useful for passing around credentials.
     */
    val requestHeaders: Map<String, List<String>>

    /**
     * Returns the XML.
     */
    val asString: String?
}