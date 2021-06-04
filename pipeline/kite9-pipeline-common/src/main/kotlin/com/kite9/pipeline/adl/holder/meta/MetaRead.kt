package com.kite9.pipeline.adl.holder.meta

import com.kite9.k9server.pipeline.uri.URI


interface MetaRead {

    val title: String
    val topicUri: URI
    val uri: URI

    /**
     * Information about the editing, creation etc. of the document.
     */
    val metaData: Map<String, Any>
}