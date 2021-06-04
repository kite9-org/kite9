package com.kite9.pipeline.adl.format.media

/**
 * Used for CSS, XML, JS.
 *
 */
data class StaticFormat(override val extension: String, override val mediaTypes: List<MediaType>) : Format