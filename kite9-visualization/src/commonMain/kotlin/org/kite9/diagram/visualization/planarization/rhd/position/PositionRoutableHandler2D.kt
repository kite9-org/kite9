package org.kite9.diagram.visualization.planarization.rhd.position

import org.kite9.diagram.common.HintMap
import org.kite9.diagram.common.elements.RoutingInfo
import org.kite9.diagram.common.objects.BasicBounds
import org.kite9.diagram.common.objects.Bounds
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D.DPos
import org.kite9.diagram.visualization.planarization.rhd.position.Tools.contains
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Implementation of the [RoutableHandler2D] functionality, but using 2D RoutableHandler Bounds as the
 * underlying storage.
 *
 * @author robmoffat
 */
class PositionRoutableHandler2D : AbstractPositionRoutableReader(), RoutableHandler2D, Logable {

    protected var tempx: MutableMap<Any?, Bounds> = HashMap(1000)
    protected var placedx: MutableMap<Any?, Bounds?> = HashMap(1000)
    protected var tempy: MutableMap<Any?, Bounds> = HashMap(1000)
    protected var placedy: MutableMap<Any?, Bounds?> = HashMap(1000)

    var log = Kite9Log.instance(this)

    override fun getPlacedPosition(r: Any): RoutingInfo? {
        val x = placedx[r]
        val y = placedy[r]
        return if (x == null || y == null) {
            null
        } else createRouting(x, y)
    }

    override fun createRouting(x: Bounds, y: Bounds): RoutingInfo {
        return BoundsBasedPositionRoutingInfo(x, y)
    }

    override fun clearTempPositions(horiz: Boolean) {
        if (horiz) {
            tempx.clear()
        } else {
            tempy.clear()
        }
    }

    /**
     * Limits to a given side of the parent container. Since ports don't occupy corners (i.e. they
     * are on a given side) this also applies a haircut in other directions too.
     */
    override fun portEdge(d: Direction, b: Bounds, horiz: Boolean): Bounds {
        return if (horiz) {
            when (d) {
                Direction.LEFT -> BasicBounds(b.distanceMin, b.distanceMin)
                Direction.RIGHT -> BasicBounds(b.distanceMax, b.distanceMax)
                else -> b.narrow(b.size() * 0.01)
            }
        } else {
            when (d) {
                Direction.UP -> BasicBounds(b.distanceMin, b.distanceMin)
                Direction.DOWN -> BasicBounds(b.distanceMax, b.distanceMax)
                else -> b.narrow(b.size() * 0.01)
            }
        }
    }

    override fun narrow(d: Layout?, using: Bounds, horiz: Boolean): Bounds {
        val multiplicationFrame = getMultiplicationFrame(d, horiz)
        val bbounds = using as BasicBounds?
        val gx =
                bbounds!!.distanceMin +
                        (bbounds.distanceMax - bbounds.distanceMin) *
                                multiplicationFrame.distanceMin
        val gw =
                (bbounds.distanceMax - bbounds.distanceMin) *
                        (multiplicationFrame.distanceMax - multiplicationFrame.distanceMin)
        return BasicBounds(gx, gx + gw)
    }

    /**
     * Given a direction, returns a rectangle with details of how the coordinates should be
     * multiplied to achieve the new orientation.
     */
    fun getMultiplicationFrame(d: Layout?, horiz: Boolean): Bounds {
        return if (d == null) {
            TOP
        } else
                when (d) {
                    Layout.LEFT -> if (horiz) TOP_HALF else TOP
                    Layout.UP -> if (horiz) TOP else TOP_HALF
                    Layout.RIGHT -> if (horiz) BOTTOM_HALF else TOP
                    Layout.DOWN -> if (horiz) TOP else BOTTOM_HALF
                    else -> TOP
                }
    }

    override fun getPosition(r: Any, horiz: Boolean): Bounds? {
        return if (horiz) {
            placedx[r]
        } else {
            placedy[r]
        }
    }

    override fun getTempPosition(r: Any, horiz: Boolean): Bounds? {
        return if (horiz) {
            tempx[r]
        } else {
            tempy[r]
        }
    }

    override fun getTopLevelBounds(horiz: Boolean): Bounds {
        return TOP
    }

    override fun setPlacedPosition(r: Any?, ri: Bounds, horiz: Boolean) {
        if (horiz) {
            placedx[r] = ri
        } else {
            placedy[r] = ri
        }
    }

    override fun setTempPosition(r: Any?, ri: Bounds, horiz: Boolean) {
        if (horiz) {
            tempx[r] = ri
        } else {
            tempy[r] = ri
        }
    }

    override fun outputSettings() {
        log.send("x positions: ", placedx)
        log.send("y positions: ", placedy)
    }

    override val prefix: String
        get() = "RH2D"
    override val isLoggingEnabled: Boolean
        get() = true

    override fun getBoundsOf(ri: RoutingInfo?, horiz: Boolean): Bounds {
        val pri = ri as BoundsBasedPositionRoutingInfo?
        return if (horiz) {
            pri!!.x
        } else {
            pri!!.y
        }
    }

    override fun compare(a: Any?, b: Any?, horiz: Boolean): DPos {
        val ab: Bounds
        val bb: Bounds
        if (horiz) {
            ab = if (a is BoundsBasedPositionRoutingInfo) a.x else placedx[a]!!
            bb = if (b is BoundsBasedPositionRoutingInfo) b.x else placedx[b]!!
        } else {
            ab = if (a is BoundsBasedPositionRoutingInfo) a.y else placedy[a]!!
            bb = if (b is BoundsBasedPositionRoutingInfo) b.y else placedy[b]!!
        }
        return compareBounds(ab, bb)
    }

    override fun compareBounds(ab: Bounds, bb: Bounds): DPos {
        return if (ab.distanceMax <= bb.distanceMin) {
            DPos.BEFORE
        } else if (ab.distanceMin >= bb.distanceMax) {
            DPos.AFTER
        } else {
            DPos.OVERLAP
        }
    }

    override fun setPlacedPosition(a: Any, ri: RoutingInfo) {
        setPlacedPosition(a, getBoundsOf(ri, true), true)
        setPlacedPosition(a, getBoundsOf(ri, false), false)
    }

    override fun overlaps(a: Bounds, b: Bounds): Boolean {
        val highestMin = max((a as BasicBounds).distanceMin, (b as BasicBounds).distanceMin)
        val lowestMax = min(a.distanceMax, b.distanceMax)
        return highestMin < lowestMax
    }

    override fun distance(from: RoutingInfo, to: RoutingInfo, horiz: Boolean): Double {
        val bFrom = getBoundsOf(from, horiz)
        val bTo = getBoundsOf(to, horiz)
        return bTo.distanceCenter - bFrom.distanceCenter
    }

    override fun overlaps(a: RoutingInfo, b: RoutingInfo): Boolean {
        val horizOverlap = overlaps(getBoundsOf(a, true), getBoundsOf(b, true))
        val vertOverlap = overlaps(getBoundsOf(a, false), getBoundsOf(b, false))
        return horizOverlap && vertOverlap
    }

    override fun order(a: RoutingInfo, b: RoutingInfo): Int {
        val ax = (a as BoundsBasedPositionRoutingInfo).x
        val bx = (b as BoundsBasedPositionRoutingInfo).x
        val ay = a.y
        val by = b.y
        val cx = ax.compareTo(bx)
        val cy = ay.compareTo(by)
        return if (cy == 0) {
            // System.out.println("cx = "+cx);
            cx
        } else if (cx == 0) {
            // System.out.println("cy = "+cy);
            cy
        } else if (abs(cx) < abs(cy)) {
            // System.out.println("cx = "+cx);
            cx
        } else {
            // System.out.println("cy = "+cy);
            cy
        }
    }

    override fun isWithin(area: RoutingInfo, pos: RoutingInfo): Boolean {
        val areax = (area as BoundsBasedPositionRoutingInfo).x
        val posx = (pos as BoundsBasedPositionRoutingInfo).x
        if (!contains(posx.distanceMin, posx.distanceMin, areax.distanceMin, areax.distanceMax)) {
            return false
        }
        val areay = area.y
        val posy = pos.y
        return contains(posy.distanceMin, posy.distanceMax, areay.distanceMin, areay.distanceMax)
    }

    override fun increaseBounds(a: RoutingInfo, b: RoutingInfo): RoutingInfo {
        if (isEmptyBounds(a)) {
            return b
        } else if (isEmptyBounds(b)) {
            return a
        }
        val ba = a as BoundsBasedPositionRoutingInfo
        val bb = b as BoundsBasedPositionRoutingInfo
        return BoundsBasedPositionRoutingInfo(ba.x.expand(bb.x)!!, ba.y.expand(bb.y)!!)
    }

    companion object {
        val TOP = BasicBounds(0.0, 1.0)
        private const val THIN_GUTTER = 0.001
        private const val THICK_GUTTER = 0.01
        val TOP_HALF = BasicBounds(0.0, .5)
        val BOTTOM_HALF = BasicBounds(.5, 1.0)

        /**
         * Although we have a planarization line (1d) and a set of positions (2d), there is no
         * description of how the planarization line progresses through 2d space. So, we make the
         * assumption that it always moves to the right or down as it goes forward. This assumption
         * appears to be true for all of the test cases we have constructed, but it's not guaranteed
         * by the system, it's only a result of the grid-based layout that we are using.
         */
        private const val THROW_ON_ASSUMPTION_FAIL = true
        val OVERLAP = Any()
        private const val TOLERANCE = 0.000000001

        fun eq(a: Double, b: Double): Boolean {
            return abs(a - b) < TOLERANCE
        }

        fun meq(a: Double, b: Double): Boolean {
            return a - b > -TOLERANCE
        }
    }

    override fun setHints(hm: HintMap?, bounds: RoutingInfo?) {
        val pri = bounds as PositionRoutingInfo?
        hm!!["px1"] = pri!!.getMinX().toFloat()
        hm["py1"] = pri.getMinY().toFloat()
        hm["px2"] = pri.getMaxX().toFloat()
        hm["py2"] = pri.getMaxY().toFloat()
    }

    override fun narrow(
            bounds: RoutingInfo?,
            vertexTrimX: Double,
            vertexTrimY: Double
    ): RoutingInfo? {
        val pri = bounds as BoundsBasedPositionRoutingInfo?
        return BoundsBasedPositionRoutingInfo(
                pri!!.x.narrow(vertexTrimX),
                pri.y.narrow(vertexTrimY)
        )
    }
}
