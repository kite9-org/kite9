package com.kite9.pipeline.uri

/**
 * Cross-platform encapsulation of URI functionality.
 */
interface URI {

    val path: String

    fun resolve(location: String): URI

}