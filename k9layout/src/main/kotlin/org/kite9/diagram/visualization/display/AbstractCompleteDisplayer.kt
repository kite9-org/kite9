package org.kite9.diagram.visualization.display

import org.kite9.diagram.common.elements.mapping.GeneratedLayoutBiDirectional
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.*
import org.kite9.diagram.model.position.CostedDimension2D
import org.kite9.diagram.model.position.CostedDimension2D.Companion.UNBOUNDED
import org.kite9.diagram.model.position.Dimension2D
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Direction.Companion.reverse
import kotlin.math.max

abstract class AbstractCompleteDisplayer(buffer: Boolean) : CompleteDisplayer, DiagramSizer, Logable {

    var buffer: Double

    protected var log = Kite9Log.instance(this)

    override fun getMinimumDistanceBetween(
        a: DiagramElement,
        aSide: Direction,
        b: DiagramElement,
        bSide: Direction,
        d: Direction,
        along: DiagramElement?,
        concave: Boolean
    ): Double {
        val distance = getMinimumDistanceInner(a, aSide, b, bSide, d, along, concave)
        log.send(if (log.go()) null else "Minimum distances between  $a  $aSide $b $bSide in $d is $distance")
        return max(distance, buffer)
    }

    /**
     * Given two diagram attr, separated with a gutter, figure out how much
     * space must be between them.
     * @param concave
     */
    private fun getMinimumDistanceInner(
        a: DiagramElement,
        aSide: Direction,
        b: DiagramElement,
        bSide: Direction,
        d: Direction,
        along: DiagramElement?,
        concave: Boolean
    ): Double {
        val length: Double
        length = if (a is GeneratedLayoutBiDirectional || b is GeneratedLayoutBiDirectional) {
            return 0.0
        } else if (a is Connection && b is Connection) {
            getMinimumDistanceConnectionToConnection(
                a,
                aSide,
                b,
                bSide,
                d,
                along,
                concave
            )
        } else if (a is Rectangular && b is Rectangular) {
            getMinimumDistanceRectangularToRectangular(a, aSide, b, bSide, d, along, concave)
        } else if (a is Rectangular && b is Connection) {
            getMinimumDistanceRectangularToConnection(
                a,
                aSide,
                b,
                bSide,
                d,
                along
            )
        } else if (a is Connection && b is Rectangular) {
            getMinimumDistanceRectangularToConnection(
                b,
                bSide,
                a,
                aSide,
                reverse(d)!!,
                along
            )
        } else {
            throw LogicException("Don't know how to calc min distance")
        }
        return length
    }

    private fun getMinimumDistanceRectangularToConnection(
        a: Rectangular,
        aSide: Direction,
        b: Connection,
        bSide: Direction,
        d: Direction,
        along: DiagramElement?
    ): Double {
        return if (aSide === d) {
            // we are outside a
            if (isEventualParent(a, b)) {
                0.0
            } else {
                val inset = getMargin(a, aSide)
                val margin = getMargin(b, bSide)
                val length = max(inset, margin)
                incorporateAlongMinimumLength(along, d, length, a, aSide, b, bSide)
            }
        } else {
            if (a is Connected && b.meets(a)) {
                // a connection arriving at a rectangular
                incorporateAlongMinimumLength(along, d, 0.0, a, aSide, b, bSide)
            } else {
                // we are inside a, so use the padding distance
                val inset = getPadding(a, d)
                val margin = getMargin(b, reverse(d)!!)
                val length = max(inset, margin)
                incorporateAlongMinimumLength(along, d, length, a, aSide, b, bSide)
            }
        }
    }

    private fun incorporateAlongMinimumLength(
        along: DiagramElement?,
        d: Direction,
        `in`: Double,
        a: DiagramElement,
        aSide: Direction,
        b: DiagramElement,
        bSide: Direction
    ): Double {
        return if (along is Connection) {
            if (passingThrough(along, a, b)) {
                // in this special case, the link can pass 
                return `in`
            }
            val alongDist = getAlongMinimumLength(along, d, a, aSide, b, bSide)
            max(`in`, alongDist)
        } else if (along is Connected) {
            val alongDist = getAlongMinimumLength(along, d, a, aSide, b, bSide)
            max(`in`, alongDist)
        } else {
            `in`
        }
    }

    private fun passingThrough(along: Connection, a: DiagramElement, b: DiagramElement): Boolean {
        val touchesA = a is Connected && along.meets(a)
        val touchesB = b is Connected && along.meets(b)
        return !touchesA && !touchesB
    }

    private fun getAlongMinimumLength(
        along: Connection,
        d: Direction?,
        a: DiagramElement,
        aSide: Direction,
        b: DiagramElement,
        bSide: Direction
    ): Double {
        val starting = along.getFrom() === a || along.getFrom() === b
        val ending = along.getTo() === b || along.getTo() === a
        return getLinkMinimumLength(along, starting, ending)
    }

    private fun getAlongMinimumLength(
        along: Connected,
        d: Direction,
        a: DiagramElement,
        aSide: Direction,
        b: DiagramElement,
        bSide: Direction
    ): Double {
        return if (along === a && b is Connection) {
            // link meeting connected, and we're working out distance to corner.
            getLinkInset(along, d)
        } else if (along === b && a is Connection) {
            // link meeting connected, and we're working out distance to corner.
            getLinkInset(along, d)
        } else if (a is Connection && b is Connection) {
            // the gutter space between two connections arriving on a side
            val startA =
                if (a.meets(along)) a.getDecorationForEnd(
                    along
                ) else null
            val startB =
                if (b.meets(along)) b.getDecorationForEnd(
                    along
                ) else null
            getLinkGutter(along, startA, aSide, startB, bSide)
        } else {
            // sides of a rectangle or something
            0.0
        }
    }

    private fun getMinimumDistanceRectangularToRectangular(
        a: Rectangular,
        aSide: Direction,
        b: Rectangular,
        bSide: Direction,
        d: Direction,
        along: DiagramElement?,
        concave: Boolean
    ): Double {
        // distances when one element is contained within another
        val length: Double
        length = if (a === b) {
            getInternalDistance(a, aSide, bSide)
        } else if (isImmediateParent(b, a)) {
            max(getPadding(a, aSide), getMargin(b, bSide))
        } else if (isImmediateParent(a, b)) {
            max(getPadding(b, bSide), getMargin(a, aSide))
        } else if (concave) {
            if (aSide === bSide) {
                // not facing each other
                0.0
            } else {
                // no containment, just near each other
                calculateMargin(a, aSide, b, bSide)
            }
        } else {
            0.0
        }
        return incorporateAlongMinimumLength(along, d, length, a, aSide, b, bSide)
    }

    protected fun isImmediateParent(a: DiagramElement, parent: DiagramElement): Boolean {
        return (a as Rectangular).getContainer() === parent
    }

    protected fun isEventualParent(d: DiagramElement?, parent: DiagramElement): Boolean {
        return if (d == null) {
            false
        } else if (d.getParent() === parent) {
            true
        } else {
            isEventualParent(d.getParent(), parent)
        }
    }

    private fun getMinimumDistanceConnectionToConnection(
        a: Connection,
        aSide: Direction,
        b: Connection,
        bSide: Direction,
        d: Direction,
        along: DiagramElement?,
        concave: Boolean
    ): Double {
        if (a === b) {
            return 0.0
        }
        var margin: Double = if (concave) calculateMargin(a, aSide, b, bSide) else 0.0
        margin = incorporateAlongMinimumLength(along, d, margin, a, aSide, b, bSide)
        return margin
    }

    private fun calculateMargin(
        a: DiagramElement,
        aSide: Direction,
        b: DiagramElement,
        bSide: Direction
    ): Double {
        val marginA = getMargin(a, aSide)
        val marginB = getMargin(b, bSide)
        return max(marginA, marginB)
    }

    private fun getInternalDistance(a: DiagramElement?, aSide: Direction?, bSide: Direction?): Double {
        return if (a == null) {
            throw LogicException("Can't get internal distance for null")
        } else if (aSide == null || bSide == null) {
            throw LogicException("Don't know sides")
        } else if (aSide === Direction.LEFT || aSide === Direction.RIGHT) {
            size(a, UNBOUNDED).w
        } else {
            size(a, UNBOUNDED).h
        }
    }

    abstract fun getPadding(a: DiagramElement, d: Direction): Double
    abstract fun getMargin(element: DiagramElement, d: Direction): Double
    protected abstract fun size(a: DiagramElement, s: Dimension2D): CostedDimension2D

    /**
     * The smallest possible length of element, when the element is starting or ending in the length being considered.
     * This should include terminators.
     */
    protected abstract fun getLinkMinimumLength(element: Connection, starting: Boolean, ending: Boolean): Double

    /**
     * Distance from the edge of a connected element to the connection, minimum.  (Could be increased by terminators)
     */
    protected abstract fun getLinkInset(element: Connected, d: Direction): Double

    /**
     * This is the amount of space along the side of "along" that should be reserved between two
     * connections.   Should also consider the amount of room required for the terminators.
     */
    protected abstract fun getLinkGutter(
        along: Connected,
        a: Terminator?,
        aSide: Direction?,
        b: Terminator?,
        bSide: Direction?
    ): Double

    override val prefix: String
        get() = "DD  "
    override val isLoggingEnabled: Boolean
        get() = true


    /**
     * Set buffer > 0 to ensure gaps even around invisible attr.  0 should be used for correct rendering
     */
    init {
        this.buffer = if (buffer) 12.0 else 0.toDouble()
    }
}