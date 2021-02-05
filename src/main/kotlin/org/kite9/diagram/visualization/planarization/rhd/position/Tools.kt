package org.kite9.diagram.visualization.planarization.rhd.position

object Tools {

    private fun pointIntersect(v: Double, min: Double, max: Double): Boolean {
        return min <= v && max >= v
    }


	fun contains(amin: Double, amax: Double, bmin: Double, bmax: Double): Boolean {
        return pointIntersect(amin, bmin, bmax) &&
                pointIntersect(amax, bmin, bmax)
    }
}