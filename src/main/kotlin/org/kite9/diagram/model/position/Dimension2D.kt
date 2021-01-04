package org.kite9.diagram.model.position

interface Dimension2D {
    val width: Double
    val height: Double
    fun size(): Dimension2D

    fun x(): Double {
        return width
    }

    fun y(): Double {
        return height
    }

    fun width(): Double {
        return width
    }

    fun height(): Double {
        return height
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
            Math.ceil(width() / factor.width()) * factor.width(),
            Math.ceil(height() / factor.height()) * factor.height()
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