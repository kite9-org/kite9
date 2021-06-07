package com.kite9.pipeline.adl.holder.pipeline

import com.kite9.pipeline.adl.holder.meta.BasicMeta
import com.kite9.pipeline.uri.K9URI

abstract class AbstractXMLBase (
    override val uri: K9URI,
    override val requestHeaders: Map<String, List<String>>,
    val meta: MutableMap<String, Any>
) : BasicMeta(meta), XMLBase