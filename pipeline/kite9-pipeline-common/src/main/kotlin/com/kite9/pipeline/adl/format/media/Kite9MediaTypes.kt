package com.kite9.pipeline.adl.format.media

/**
 * Defines many media types used by kite9.
 *
 * @author robmoffat
 */
object Kite9MediaTypes {

    // plain text
    const val TEXT_PLAIN_VALUE = "text/plain"

    // all
    const val ALL_VALUE = "*/*"

    // json
    const val HAL_JSON_VALUE = "application/hal+json"
    const val APPLICATION_JSON_VALUE = "application/json"

    // xml
    const val TEXT_XML_VALUE = "text/xml"
    const val APPLICATION_XML_VALUE = "application/xml"

    // diagram formats
    const val ADL_XML_VALUE = "text/xml;purpose=adl" // input ADL
    const val EDITABLE_SVG_VALUE = "image/svg+xml;purpose=editable" // editable SVG, with references to fonts and stylesheets.
    const val PNG_VALUE = "image/png"
    const val SVG_VALUE = "image/svg+xml" // output SVG, all styles/images encapsulated
    const val PDF_VALUE = "application/pdf" // output PDF
    const val CLIENT_SIDE_IMAGE_MAP_VALUE = "text/html-image-map" // output html-fragment
    const val HTML_VALUE = "text/html" // output html
    const val JAVASCRIPT_VALUE = "text/javascript" // JS
    const val CSS_VALUE = "text/css" // CSS
    const val XSLT_VALUE = "application/xslt+xml"
    const val SEF_VALUE = "application/xslt+json"
    const val JPEG_VALUE = "image/jpeg"


    // fonts
    const val WOFF_VALUE = "font/woff"
    const val WOFF2_VALUE = "font/woff2"
    const val TTF_VALUE = "font/ttf"
    const val OTF_VALUE = "font/otf"
    const val EOT_VALUE = "application/vnd.ms-fontobject"


    val SVG: K9MediaType = K9MediaType.parseMediaType(SVG_VALUE)
    val XSLT: K9MediaType = K9MediaType.parseMediaType(XSLT_VALUE)
    val SEF: K9MediaType = K9MediaType.parseMediaType(SEF_VALUE)
    val ESVG: K9MediaType = K9MediaType.parseMediaType(EDITABLE_SVG_VALUE)
    val PNG: K9MediaType = K9MediaType.parseMediaType(PNG_VALUE)
    val JPEG: K9MediaType  = K9MediaType.parseMediaType(JPEG_VALUE)
    val PDF: K9MediaType = K9MediaType.parseMediaType(PDF_VALUE)
    val ADL_SVG = K9MediaType.parseMediaType(ADL_XML_VALUE)
    val CLIENT_SIDE_IMAGE_MAP = K9MediaType.parseMediaType(CLIENT_SIDE_IMAGE_MAP_VALUE)
    val JS = K9MediaType.parseMediaType(JAVASCRIPT_VALUE)
    val CSS = K9MediaType.parseMediaType(CSS_VALUE)
    val APPLICATION_JSON = K9MediaType.parseMediaType(APPLICATION_JSON_VALUE)
    val HAL_JSON = K9MediaType.parseMediaType(HAL_JSON_VALUE)
    val TEXT_XML = K9MediaType.parseMediaType(APPLICATION_XML_VALUE)
    val APPLICATION_XML = K9MediaType.parseMediaType(TEXT_XML_VALUE)
    val WOFF: K9MediaType = K9MediaType.parseMediaType(WOFF_VALUE)
    val WOFF2: K9MediaType = K9MediaType.parseMediaType(WOFF2_VALUE)
    val TTF: K9MediaType = K9MediaType.parseMediaType(TTF_VALUE)
    val EOT: K9MediaType = K9MediaType.parseMediaType(EOT_VALUE)
    val OTF: K9MediaType = K9MediaType.parseMediaType(OTF_VALUE)
    val HTML : K9MediaType = K9MediaType.parseMediaType(HTML_VALUE)
    val ALL: K9MediaType = K9MediaType.parseMediaType(ALL_VALUE)
}