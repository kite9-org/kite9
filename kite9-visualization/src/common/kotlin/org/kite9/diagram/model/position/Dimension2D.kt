package org.kite9.diagram.model.position

import kotlin.math.ceil

interface Dimension2D {

    val w: Double
    val h: Double
    fun size(): Dimension2D

    fun x(): Double {
        return w
    }

    fun y(): Double {
        return h
    }

    fun width(): Double {
        return w
    }

    fun height(): Double {
        return h
    }

    fun add(by: Dimension2D): Dimension2D {
        val d2 = BasicDimension2D(width() + by.width(), height() + by.height())
        return d2
    }

    fun minus(by: Dimension2D): Dimension2D {
        val d2 = BasicDimension2D(width() - by.width(), height() - by.height())
        return d2
    }


    fun setY(y: Double): Dimension2D {
        return BasicDimension2D(width(), y)
    }

    fun setX(x: Double): Dimension2D {
        return BasicDimension2D(x, height())
    }
}