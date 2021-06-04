package com.kite9.pipeline.adl.holder

import com.kite9.k9server.pipeline.adl.holder.pipeline.ADLBase
import com.kite9.k9server.pipeline.adl.holder.pipeline.ADLDom
import com.kite9.k9server.pipeline.uri.URI

interface ADLFactory {

    fun uri(uri: URI, requestHeaders: Map<String, List<String>>): ADLBase

    fun adl(uri: URI, xml: String, requestHeaders: Map<String, List<String>>): ADLBase

    /**
     * This is currently just used for HateoasADLHttpMessageWriter, and really shouldn't be,
     * since we aren't giving it the content.
     */
    @Deprecated("Remove this after conversion to xslt")
    fun emptyAdlDom(uri: URI, requestHeaders: Map<String, List<String>>): ADLDom

    /**
     * Used for HateoasADLHttpMessageWriter, probably shouldn't be.
     */
    @Deprecated("Remove this after conversion to xslt")
    fun loadText(uri2: URI?, requestHeaders: Map<String?, List<String?>?>?): String?
}