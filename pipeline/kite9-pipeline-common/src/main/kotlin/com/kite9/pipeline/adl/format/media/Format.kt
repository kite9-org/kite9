package com.kite9.pipeline.adl.format.media

/**
 * Handles sending a certain file format to the output stream for http
 * responses.
 *
 * @author robmoffat
 */
interface Format {

    /**
     * A canonical media type, used by Kite9 server consistently.
     * Used for caching.
     */
    val formatIdentifier: String

    val mediaTypes: List<MediaType>

    /**
     * In order to use format=xxx in the URL, we need to give each format an extension.
     */
    val extension: String?
}