package com.kite9.k9server.adl.format.media

/**
 * Defines many media types used by kite9.
 *
 * @author robmoffat
 */
object Kite9MediaTypes {
    // json
    const val HAL_JSON_VALUE = "application/hal+json"
    const val APPLICATION_JSON_VALUE = "application/json"

    public val HAL_JSON: MediaType
    val APPLICATION_JSON: MediaType

    // xml
    const val TEXT_XML_VALUE = "text/xml"
    const val APPLICATION_XML_VALUE = "application/xml"

    val TEXT_XML: MediaType
    val APPLICATION_XML: MediaType

    // diagram formats
    const val ADL_SVG_VALUE = "text/adl-svg+xml" // input ADL
    const val EDITABLE_SVG_VALUE = "image/svg+xml;purpose=editable" // editable SVG, with references to fonts and stylesheets.
    const val PNG_VALUE = "image/png"
    const val SVG_VALUE = "image/svg+xml" // output SVG, all styles/images encapsulated
    const val PDF_VALUE = "application/pdf" // output PDF
    const val CLIENT_SIDE_IMAGE_MAP_VALUE = "text/html-image-map" // output html-fragment
    const val JAVASCRIPT_VALUE = "text/javascript" // JS
    const val CSS_VALUE = "text/css" // CSS
    const val XSLT_VALUE = "application/xslt+xml"
    const val JPEG_VALUE = "image/jpeg"

    val SVG: MediaType
    val XSLT: MediaType
    val ESVG: MediaType
    val PNG: MediaType
    val JPEG: MediaType
    val PDF: MediaType
    val ADL_SVG: MediaType
    val CLIENT_SIDE_IMAGE_MAP: MediaType
    val JS: MediaType
    val CSS: MediaType

    // fonts
    const val WOFF_VALUE = "font/woff"
    const val WOFF2_VALUE = "font/woff2"
    const val TTF_VALUE = "font/ttf"
    const val OTF_VALUE = "font/otf"
    const val EOT_VALUE = "application/vnd.ms-fontobject"

    // fonts
    val WOFF: MediaType
    val WOFF2: MediaType
    val TTF: MediaType
    val EOT: MediaType
    val OTF: MediaType

    init {
        SVG = MediaType.parseMediaType(SVG_VALUE)
        ESVG = MediaType.parseMediaType(EDITABLE_SVG_VALUE)
        PDF = MediaType.parseMediaType(PDF_VALUE)
        PNG = MediaType.parseMediaType(PNG_VALUE)
        JPEG = MediaType.parseMediaType(JPEG_VALUE)
        ADL_SVG = MediaType.parseMediaType(ADL_SVG_VALUE)
        CLIENT_SIDE_IMAGE_MAP = MediaType.parseMediaType(CLIENT_SIDE_IMAGE_MAP_VALUE)
        JS = MediaType.parseMediaType(JAVASCRIPT_VALUE)
        CSS = MediaType.parseMediaType(CSS_VALUE)
        WOFF = MediaType.parseMediaType(WOFF_VALUE)
        WOFF2 = MediaType.parseMediaType(WOFF2_VALUE)
        TTF = MediaType.parseMediaType(TTF_VALUE)
        EOT = MediaType.parseMediaType(EOT_VALUE)
        OTF = MediaType.parseMediaType(OTF_VALUE)
        XSLT = MediaType.parseMediaType(XSLT_VALUE)
        APPLICATION_JSON = MediaType.parseMediaType(APPLICATION_JSON_VALUE)
        HAL_JSON = MediaType.parseMediaType(HAL_JSON_VALUE)
        TEXT_XML = MediaType.parseMediaType(APPLICATION_XML_VALUE)
        APPLICATION_XML = MediaType.parseMediaType(TEXT_XML_VALUE)
    }
}