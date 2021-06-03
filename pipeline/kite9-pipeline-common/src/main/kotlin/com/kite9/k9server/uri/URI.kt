package com.kite9.k9server.uri

/**
 * Cross-platform encapsulation of URI functionality.
 */
interface URI {

    val path: String

    fun resolve(location: String): URI

}