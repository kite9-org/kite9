package com.kite9.pipeline.adl.format.media

/**
 * Used for CSS, XML, JS.
 *
 */
data class StaticFormat(override val extension: String, override val mediaTypes: List<K9MediaType>) : Format {

    override val formatIdentifier: String
        get() =  mediaTypes[0].toString()

}