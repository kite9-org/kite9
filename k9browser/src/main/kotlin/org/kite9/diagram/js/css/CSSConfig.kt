package org.kite9.diagram.js.css

import org.kite9.diagram.dom.css.CSSConstants
import kotlinx.browser.*

data class CSSProperty(val name: String, val syntax: String, val initialValue: String, val inherits: Boolean)

external interface CSSRegistry {

    fun registerProperty(p: CSSProperty)


}

class CSSConfig {


}

fun init() {

    val css = window.asDynamic().CSS as CSSRegistry
    css.registerProperty( CSSProperty(CSSConstants.PADDING_LEFT_PROPERTY, "<length>", "0px", false));
    css.registerProperty( CSSProperty(CSSConstants.PADDING_RIGHT_PROPERTY, "<length>", "0px", false));
    css.registerProperty( CSSProperty(CSSConstants.PADDING_BOTTOM_PROPERTY, "<length>", "0px", false));
    css.registerProperty( CSSProperty(CSSConstants.PADDING_TOP_PROPERTY, "<length>", "0px", false));

}