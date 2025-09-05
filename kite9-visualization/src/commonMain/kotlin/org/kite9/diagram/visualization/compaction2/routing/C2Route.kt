package org.kite9.diagram.visualization.compaction2.routing

import org.kite9.diagram.common.algorithms.ssp.PathLocation
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.visualization.compaction2.C2Slideable

data class C2Route(val prev: C2Route?, val coords: C2Coords, val point: C2Point, val cost: C2Costing, val container: DiagramElement) : PathLocation<C2Route> {

    constructor(r: C2Route?, point: C2Point, cost: C2Costing, container: DiagramElement) : this(r, buildCoords(r, point), point, cost, container)
    constructor(r: C2Route, point: C2Point, cost: C2Costing) : this(simplifyRoute(r, point), buildCoords(r, point), point, cost, r.container)
    constructor(r: C2Route, cost: C2Costing, container: DiagramElement) : this(simplifyRoute(r, r.point), buildCoords(r, r.point), r.point, cost, container)

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
        return "Route(points=${getPoints()}, cost=$cost, coords=$coords active=$active container=$container)"
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

    fun changeContainer(container: DiagramElement, c2: C2Costing): C2Route {
        return C2Route(this, c2, container)
    }

    companion object {

        private fun buildCoords(r: C2Route?, point: C2Point) : C2Coords {
            return if (r == null) {
                C2Coords.createInitialCoords(point)
            } else {
                C2Coords.createFollowingCoords(r.coords, point)
            }
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

    }

}