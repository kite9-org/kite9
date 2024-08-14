package org.kite9.diagram.visualization.compaction2.routing

import C2Costing
import org.kite9.diagram.common.algorithms.ssp.PathLocation

class C2Route : PathLocation<C2Route> {

    val prev: C2Route?
    val point: C2Point
    val cost: C2Costing
    constructor(r: C2Route?, point: C2Point, cost: C2Costing) {
        this.prev = simplifyRoute(r, point)
        this.point = point
        this.cost = cost
    }

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
        return "Route(points=${getPoints()}, cost=$cost, active=$active)"

    }

    private fun getPoints() : List<C2Point> {
        val rest = if (this.prev == null) emptyList() else prev.getPoints()
        return listOf(this.point).plus(rest)
    }

}