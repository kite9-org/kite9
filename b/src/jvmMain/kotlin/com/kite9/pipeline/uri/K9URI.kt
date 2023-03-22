package com.kite9.pipeline.uri

/**
 * Cross-platform encapsulation of URI functionality.
 */
interface K9URI {

    val scheme: String
    val host: String
    val path: String

    fun resolve(location: String): K9URI

    fun changeScheme(scheme: String, path: String): K9URI
    fun withoutQueryParameters(): K9URI
    
    fun param(p: String) : List<String>
}