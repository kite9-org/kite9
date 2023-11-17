package org.kite9.diagram.visualization.compaction2.routing

import org.kite9.diagram.common.algorithms.ssp.PathLocation

class C2Route(val prev: C2Route?, val point: C2Point, val cost: Int) : PathLocation<C2Route> {

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
        return "Route(point=$point, cost=$cost, active=$active)"
    }


}