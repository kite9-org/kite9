package org.kite9.diagram.model.position

import kotlin.math.max
import kotlin.math.min

class RouteRenderingInformationImpl : AbstractRenderingInformationImpl(), RouteRenderingInformation {

    override val routePositions: MutableList<Dimension2D> = mutableListOf()
    override val hops: MutableList<Boolean> = mutableListOf()
    override var isContradicting = false

    override fun isHop(pos: Int): Boolean {
        return if (pos >= hops.size) false else hops[pos]
    }

    fun setHop(pos: Int) {
        while (hops.size <= pos) {
            hops.add(false)
        }
        hops[pos] = true
    }

    override fun getWaypoint(pos: Int): Dimension2D? {
        return routePositions[pos]
    }

    override fun clear() {
        routePositions.clear()
        hops.clear()
    }

    override fun getLength(): Int {
        return routePositions.size
    }

    override fun add(d: Dimension2D) {
        routePositions.add(d)
    }

    private fun ensureSizeAndPosition() {
        var minx = Double.MAX_VALUE
        var maxx = 0.0
        var miny = Double.MAX_VALUE
        var maxy = 0.0
        for (d in routePositions) {
            minx = min(d.x(), minx)
            miny = min(d.y(), miny)
            maxx = max(d.x(), maxx)
            maxy = max(d.y(), maxy)
        }
        position = BasicDimension2D(minx, miny)
        size = BasicDimension2D(maxx - minx, maxy - miny)
    }

    override var position: Dimension2D?
        get() {
            ensureSizeAndPosition()
            return super.position
        }
        set(position) {
            super.position = position
        }

    override var size: Dimension2D?
        get() {
            ensureSizeAndPosition()
            return super.size
        }
        set(size) {
            super.size = size
        }
}