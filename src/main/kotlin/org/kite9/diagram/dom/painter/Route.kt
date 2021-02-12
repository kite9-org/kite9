package org.kite9.diagram.dom.painter

interface Route {

    fun moveTo(x1: Double, y1: Double)
    fun lineTo(x1: Double, y1: Double)
    fun closePath()
    fun arc(x1: Double, y1: Double, w: Double, h: Double, clockwise: Boolean);

}