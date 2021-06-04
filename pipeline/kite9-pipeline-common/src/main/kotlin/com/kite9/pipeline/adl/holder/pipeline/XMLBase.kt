package com.kite9.pipeline.adl.holder.pipeline

import com.kite9.k9server.pipeline.uri.URI

/**
 * Base interface that captures where an xml document came from, and it's content.
 */
interface XMLBase {

    val uri: URI

    /**
     * Returns the HTTPHeaders that were responsible for loading this
     * ADL.  Useful for passing around credentials.
     */
    val requestHeaders: Map<String, List<String>>

    /**
     * Returns the XML.
     */
    val xMLString: String
}