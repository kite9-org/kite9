package org.kite9.diagram.visualization.compaction2.routing

import org.kite9.diagram.common.algorithms.ssp.PathLocation
import org.kite9.diagram.visualization.compaction2.C2Slideable

class C2Route(r: C2Route?, val point: C2Point, val cost: C2Costing) : PathLocation<C2Route> {

    val prev: C2Route?
    val coords: C2Coords

    /**
     * Removes parts of the route in the same direction, so we only record changes
     * in direction
     */
    private fun simplifyRoute(r: C2Route?, point: C2Point): C2Route? {
        var out = r
        while ((out != null) && (out.prev != null) && (out.point.d == point.d)) {
            out = out.prev
        }

        return out
    }

    private var active = true

    override fun compareTo(other: C2Route): Int {
        return cost.compareTo(other.cost)
    }

    override fun getLocation(): Any {
        return point
    }

    override fun isActive(): Boolean {
        return active
    }

    override fun setActive(b: Boolean) {
        this.active = b
    }

    override fun toString(): String {
        return "Route(points=${getPoints()}, cost=$cost, coords=$coords active=$active)"

    }

    private fun getPoints() : List<C2Point> {
        val rest = if (this.prev == null) emptyList() else prev.getPoints()
        return listOf(this.point).plus(rest)
    }

    fun getSlideables() :  List<C2Slideable> {
        return getPoints().flatMap { p -> listOf(p.b, p.a) }.distinct()
    }

    fun explainRoute(): List<C2Route> {
        return if (prev != null) {
            this.prev.explainRoute().plus(this)
        } else {
            listOf(this)
        }
    }

    init {
        this.prev = simplifyRoute(r, point)
        this.coords = if (r == null) C2Coords.createInitialCoords(point) else C2Coords.createFollowingCoords(r.coords, point)
    }

}