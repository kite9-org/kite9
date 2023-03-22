package com.kite9.pipeline.adl.holder

import com.kite9.pipeline.adl.holder.pipeline.ADLBase
import com.kite9.pipeline.adl.holder.pipeline.ADLDom
import com.kite9.pipeline.uri.K9URI
import org.w3c.dom.Document

interface ADLFactory {

    fun uri(uri: K9URI, requestHeaders: Map<String, List<String>>): ADLBase

    fun adl(uri: K9URI, xml: String, requestHeaders: Map<String, List<String>>): ADLBase

    fun dom(uri: K9URI, doc: Document, requestHeaders: Map<String, List<String>>) : ADLDom

}