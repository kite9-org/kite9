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

    fun divide(by: Dimension2D): Dimension2D {
        val d2 = BasicDimension2D(width() / by.width(), height() / by.height())
        return d2
    }

    fun multiply(by: Dimension2D): Dimension2D {
        val d2 = BasicDimension2D(width() * by.width(), height() * by.height())
        return d2
    }

    fun multiply(by: Double): Dimension2D {
        val d2 = BasicDimension2D(width() * by, height() * by)
        return d2
    }

    fun roundUpTo(factor: Dimension2D): Dimension2D {
        val d2 = BasicDimension2D(
            ceil(width() / factor.width()) * factor.width(),
            ceil(height() / factor.height()) * factor.height()
        )
        return d2
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