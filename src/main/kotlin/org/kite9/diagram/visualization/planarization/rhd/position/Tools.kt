package org.kite9.diagram.visualization.planarization.rhd.position

import java.awt.geom.Rectangle2D

object Tools {
    
    private fun pointIntersect(v: Double, min: Double, max: Double): Boolean {
        return min <= v && max >= v
    }

    fun contains(container: Rectangle2D, item: Rectangle2D): Boolean {
        return pointIntersect(item.minX, container.minX, container.maxX) &&
                pointIntersect(item.maxX, container.minX, container.maxX) &&
                pointIntersect(item.minY, container.minY, container.maxY) &&
                pointIntersect(item.maxY, container.minY, container.maxY)
    }

    @JvmStatic
	fun contains(amin: Double, amax: Double, bmin: Double, bmax: Double): Boolean {
        return pointIntersect(amin, bmin, bmax) &&
                pointIntersect(amax, bmin, bmax)
    }
}