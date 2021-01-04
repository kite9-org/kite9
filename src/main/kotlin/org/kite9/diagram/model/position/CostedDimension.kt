package org.kite9.diagram.model.position

import org.kite9.diagram.model.DiagramElement

/**
 * This extends the idea of dimension, but allows you to associate a cost with the dimension
 * other than simply the size.
 *
 * @author robmoffat
 */
open class CostedDimension : Dimension2D {
    var cost: Long = 0

    constructor() : super() {}
    constructor(d: Dimension2D) : super(d.width, d.height) {}
    constructor(arg0: Double, arg1: Double, cost: Long) : super(arg0, arg1) {
        this.cost = cost
    }

    /**
     * Works out cost based on how well the new [CostedDimension] fits into within.
     */
    constructor(width: Double, height: Double, within: Dimension2D?) : super(width, height) {
        if (within != null) {
            val extraHeight = Math.max(height - within.height, 0.0)
            val extraWidth = Math.max(width - within.width, 0.0)
            cost = (extraHeight * width + extraWidth * height + extraHeight * extraWidth).toLong()
        }
    }

    fun setCost(cost: Int) {
        this.cost = cost.toLong()
    }

    operator fun compareTo(o: DiagramElement?): Int {
        return if (o is CostedDimension) {
            cost.compareTo((o as CostedDimension).cost)
        } else {
            0
        }
    }

    companion object {

		val ZERO: CostedDimension = CostedDimension(0.0, 0.0, 0)
        val NOT_DISPLAYABLE: CostedDimension = CostedDimension(-1.0, -1.0, Long.MAX_VALUE)
		val UNBOUNDED: CostedDimension = CostedDimension(Double.MAX_VALUE, Double.MAX_VALUE, 0)

        fun chooseBest(a: CostedDimension, b: CostedDimension): CostedDimension {
            return if (a === NOT_DISPLAYABLE) {
                if (b === NOT_DISPLAYABLE) {
                    NOT_DISPLAYABLE
                } else b
            } else {
                if (a.cost < b.cost) {
                    return a
                } else if (a.cost > b.cost) {
                    return b
                }
                if (a.height < b.height) {
                    return a
                } else if (a.height > b.height) {
                    return b
                }
                if (a.width < b.width) {
                    return a
                } else if (a.width > b.width) {
                    return b
                }
                a
            }
        }
    }
}