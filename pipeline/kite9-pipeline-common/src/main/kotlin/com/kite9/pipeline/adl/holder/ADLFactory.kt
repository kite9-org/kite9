package com.kite9.pipeline.adl.holder

import com.kite9.pipeline.adl.holder.pipeline.ADLBase
import com.kite9.pipeline.uri.K9URI

interface ADLFactory {

    fun uri(uri: K9URI, requestHeaders: Map<String, List<String>>): ADLBase

    fun adl(uri: K9URI, xml: String, requestHeaders: Map<String, List<String>>): ADLBase

}