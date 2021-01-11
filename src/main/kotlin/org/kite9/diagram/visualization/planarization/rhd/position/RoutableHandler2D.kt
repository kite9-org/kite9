package org.kite9.diagram.visualization.planarization.rhd.position

import org.kite9.diagram.common.HintMap
import org.kite9.diagram.common.elements.RoutingInfo
import org.kite9.diagram.common.objects.Bounds
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.visualization.planarization.mgt.router.RoutableReader

/**
 * Manages the position of objects in 2D space by considering 2D position individually.
 * This extends [RoutableReader] by adding the ability to set positions in a given
 * axis.
 *
 * @author robmoffat
 */
interface RoutableHandler2D : RoutableReader {

    enum class DPos {
        BEFORE, OVERLAP, AFTER
    }

    fun getPosition(r: Any, horiz: Boolean): Bounds?
    fun getTempPosition(r: Any, horiz: Boolean): Bounds?
    fun setPlacedPosition(r: Any?, ri: Bounds, horiz: Boolean)
    fun setTempPosition(r: Any?, ri: Bounds, horiz: Boolean)
    fun clearTempPositions(horiz: Boolean)
    fun getTopLevelBounds(horiz: Boolean): Bounds
    fun narrow(layout: Layout?, b: Bounds, horiz: Boolean, applyGutters: Boolean): Bounds
    fun createRouting(x: Bounds, y: Bounds): RoutingInfo
    fun outputSettings()
    fun getBoundsOf(ri: RoutingInfo?, horiz: Boolean): Bounds
    fun compare(a: Any?, b: Any?, horiz: Boolean): DPos
    fun compareBounds(ab: Bounds, bb: Bounds): DPos
    fun setPlacedPosition(a: Any, cri: RoutingInfo)
    override fun overlaps(a: RoutingInfo, b: RoutingInfo): Boolean
    fun overlaps(a: Bounds, b: Bounds): Boolean
    fun distance(from: RoutingInfo, to: RoutingInfo, horiz: Boolean): Double

    /**
     * Expands the area in bounding to cover ri
     */
    override fun increaseBounds(bounding: RoutingInfo, ri: RoutingInfo): RoutingInfo

    /**
     * Returns a routing which represents an empty area.
     */
    fun emptyBounds(): RoutingInfo

    /**
     * Returns true if the routing info given is the empty bounds.
     */
    fun isEmptyBounds(bounds: RoutingInfo): Boolean

    /**
     * Works out distance cost of from -> to.
     */
    fun cost(from: RoutingInfo, to: RoutingInfo): Double

    /**
     * Determines the order the two RI's must be in for the planarization line.  returns -1, 1 or 0 if same
     */
    fun order(a: RoutingInfo, b: RoutingInfo): Int

    /**
     * Useful debug method.
     */
    //fun drawPositions(out: Collection<Routable>): BufferedImage

    /**
     * Reports the bounds into the hintmap object for future renderings
     */
    fun setHints(hm: HintMap?, bounds: RoutingInfo?)

    /**
     * Trims an existing bounds by a given amount
     */
    fun narrow(bounds: RoutingInfo?, vertexTrimX: Double, vertexTrimY: Double): RoutingInfo?
}