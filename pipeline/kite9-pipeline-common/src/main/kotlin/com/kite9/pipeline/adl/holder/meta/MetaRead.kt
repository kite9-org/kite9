package com.kite9.pipeline.adl.holder.meta

import com.kite9.pipeline.uri.K9URI


interface MetaRead {

    fun getTitle(): String
    fun getTopicUri(): K9URI?
    fun getUri(): K9URI?

    /**
     * Information about the editing, creation etc. of the document.
     */
    val metaData: Map<String, Any>
}