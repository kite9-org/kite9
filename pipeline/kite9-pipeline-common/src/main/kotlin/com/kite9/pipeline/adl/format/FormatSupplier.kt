package com.kite9.pipeline.adl.format

import com.kite9.pipeline.adl.format.media.Format
import com.kite9.pipeline.adl.format.media.K9MediaType
import com.kite9.pipeline.uri.K9URI

interface FormatSupplier {

    fun getFormatFor(mt: K9MediaType): Format
    val mediaTypes: List<K9MediaType>
    val mediaTypeMap: Map<String, K9MediaType>
    fun getFormatFor(path: String): Format?
    fun getFormatFor(u: K9URI): Format?
    fun getMediaTypeFor(u: K9URI): K9MediaType
    fun getMediaTypeFor(path: String): K9MediaType
    val priorityOrderedFormats: Array<Format>

}