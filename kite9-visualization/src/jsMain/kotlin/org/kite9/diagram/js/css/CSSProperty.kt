package org.kite9.diagram.js.css

data class CSSProperty(@JsName("name") val name: String,
                       @JsName("syntax")val syntax: String,
                       @JsName("initialValue") val initialValue: String?,
                       @JsName("inherits") val inherits: Boolean)