package org.kite9.diagram.js.bridge

external class PropValue {

    val length : Int?

    val value : Any

    val unit : String?

    fun to(x: String) : PropValue
}

external class StylePropertyMapReadOnly {

    fun get(p: String) : PropValue?

}