package com.kite9.pipeline.adl.holder.meta

import com.kite9.pipeline.uri.K9URI


interface MetaRead {

    val title: String
    val topicUri: K9URI?
    val uri: K9URI?

    /**
     * Information about the editing, creation etc. of the document.
     */
    val metaData: Map<String, Any>
}