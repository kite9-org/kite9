package org.kite9.diagram.visualization.compaction2.routing

import org.kite9.diagram.common.algorithms.ssp.PathLocation
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.visualization.compaction2.C2Slideable

data class C2Route(val r: C2Route?, val point: C2Point, val cost: C2Costing, val container: DiagramElement) : PathLocation<C2Route> {

    val prev: C2Route?
    val coords: C2Coords

    constructor(r: C2Route, cost: C2Costing) : this(r, r.point, cost, r.container)
    constructor(r: C2Route, point: C2Point, cost: C2Costing) : this(r, point, cost, r.container)
    constructor(r: C2Route, cost: C2Costing, container: DiagramElement) : this(r, r.point, cost, container)

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

    fun changeContainer(container: DiagramElement): C2Route {
        return C2Route(this, this.cost, container)
    }

    init {
        this.prev = simplifyRoute(r, point)
        this.coords = if (r == null) C2Coords.createInitialCoords(point) else C2Coords.createFollowingCoords(r.coords, point)
    }

}