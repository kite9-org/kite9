package org.kite9.diagram.model.position

data class Rectangle2D(val x: Double, val y: Double, val width: Double, val height: Double) {


    val maxX = x + width;

    val maxY = y + height;
}