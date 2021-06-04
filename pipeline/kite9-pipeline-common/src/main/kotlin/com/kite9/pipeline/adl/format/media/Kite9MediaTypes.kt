package com.kite9.pipeline.adl.format.media

/**
 * Defines many media types used by kite9.
 *
 * @author robmoffat
 */
object Kite9MediaTypes {
    // json
    const val HAL_JSON_VALUE = "application/hal+json"
    const val APPLICATION_JSON_VALUE = "application/json"

    // xml
    const val TEXT_XML_VALUE = "text/xml"
    const val APPLICATION_XML_VALUE = "application/xml"

    // diagram formats
    const val ADL_SVG_VALUE = "text/adl-svg+xml" // input ADL
    const val EDITABLE_SVG_VALUE = "image/svg+xml;purpose=editable" // editable SVG, with references to fonts and stylesheets.
    const val PNG_VALUE = "image/png"
    const val SVG_VALUE = "image/svg+xml" // output SVG, all styles/images encapsulated
    const val PDF_VALUE = "application/pdf" // output PDF
    const val CLIENT_SIDE_IMAGE_MAP_VALUE = "text/html-image-map" // output html-fragment
    const val HTML_VALUE = "text/html" // output html
    const val JAVASCRIPT_VALUE = "text/javascript" // JS
    const val CSS_VALUE = "text/css" // CSS
    const val XSLT_VALUE = "application/xslt+xml"
    const val JPEG_VALUE = "image/jpeg"

    // fonts
    const val WOFF_VALUE = "font/woff"
    const val WOFF2_VALUE = "font/woff2"
    const val TTF_VALUE = "font/ttf"
    const val OTF_VALUE = "font/otf"
    const val EOT_VALUE = "application/vnd.ms-fontobject"


    val SVG: MediaType = MediaType.parseMediaType(SVG_VALUE)
    val XSLT: MediaType = MediaType.parseMediaType(XSLT_VALUE)
    val ESVG: MediaType = MediaType.parseMediaType(EDITABLE_SVG_VALUE)
    val PNG: MediaType = MediaType.parseMediaType(PNG_VALUE)
    val JPEG: MediaType  = MediaType.parseMediaType(JPEG_VALUE)
    val PDF: MediaType = MediaType.parseMediaType(PDF_VALUE)
    val ADL_SVG = MediaType.parseMediaType(ADL_SVG_VALUE)
    val CLIENT_SIDE_IMAGE_MAP = MediaType.parseMediaType(CLIENT_SIDE_IMAGE_MAP_VALUE)
    val JS = MediaType.parseMediaType(JAVASCRIPT_VALUE)
    val CSS = MediaType.parseMediaType(CSS_VALUE)
    val APPLICATION_JSON = MediaType.parseMediaType(APPLICATION_JSON_VALUE)
    val HAL_JSON = MediaType.parseMediaType(HAL_JSON_VALUE)
    val TEXT_XML = MediaType.parseMediaType(APPLICATION_XML_VALUE)
    val APPLICATION_XML = MediaType.parseMediaType(TEXT_XML_VALUE)
    val WOFF: MediaType = MediaType.parseMediaType(WOFF_VALUE)
    val WOFF2: MediaType = MediaType.parseMediaType(WOFF2_VALUE)
    val TTF: MediaType = MediaType.parseMediaType(TTF_VALUE)
    val EOT: MediaType = MediaType.parseMediaType(EOT_VALUE)
    val OTF: MediaType = MediaType.parseMediaType(OTF_VALUE)
    val HTML : MediaType = MediaType.parseMediaType(HTML_VALUE)
}