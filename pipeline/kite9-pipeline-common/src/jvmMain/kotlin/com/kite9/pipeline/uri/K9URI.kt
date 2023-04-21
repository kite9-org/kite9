package com.kite9.pipeline.uri

import java.util.function.Predicate

/**
 * Cross-platform encapsulation of URI functionality.
 */
interface K9URI {

    val scheme: String
    val host: String
    val path: String

    fun resolve(location: String): K9URI

    /**
     * Used for creating the Websocket URI
     */
    fun changeScheme(scheme: String, path: String): K9URI

    fun param(p: String): List<String>
}