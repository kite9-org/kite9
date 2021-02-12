package org.kite9.diagram.dom.painter

import kotlin.math.abs
import kotlin.math.roundToLong

class SVGRoute : Route {

    val sb = StringBuilder()

    override fun moveTo(x1: Double, y1: Double) {
        sb.append("M${toString(x1)} ${toString(y1)} ")
    }

    override fun lineTo(x1: Double, y1: Double) {
        sb.append("L${toString(x1)} ${toString(y1)} ")
    }

    override fun closePath() {
        sb.append("Z")
    }

    override fun arc(x1: Double, y1: Double, x2: Double, y2: Double, clockwise: Boolean) {
        sb.append("A")
        sb.append(toString(abs(x2-x1)))
        sb.append(" ")
        sb.append(toString(abs(y2-y1)))
        sb.append(" 0 0 ")
        sb.append(if (clockwise) "1 " else "0 ")
        sb.append(toString(x2))
        sb.append(" ")
        sb.append(toString(y2))
        sb.append(" ")
    }


    override fun toString() : String {
        return sb.toString()
    }

    fun toString(d: Double): String {
        val out = StringBuilder();
        var lx = d.roundToLong()
        return lx.toString()
    }
}