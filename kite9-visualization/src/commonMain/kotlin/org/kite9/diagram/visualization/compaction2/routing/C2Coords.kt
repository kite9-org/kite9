package org.kite9.diagram.visualization.compaction2.routing

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.compaction2.C2Slideable
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Keeps track of the bounds a C2Point could occupy
 */
data class C2Coords(val h: Int, val v: Int)  {

    fun isInBounds(s: C2Slideable, d: Direction) : Boolean {
        return when (d) {
            Direction.DOWN -> s.minimumPosition >= v
            Direction.UP -> s.minimumPosition <= v
            Direction.LEFT -> s.minimumPosition <= h
            Direction.RIGHT -> s.minimumPosition >= h
        }
    }

    fun absoluteDistance(o: C2Coords) : Int {
        return abs(this.v-o.v) + abs(this.h - o.h)
    }

    fun distanceTo(p: C2Point) : Int {
        return absoluteDistance(createFollowingCoords(this, p))
    }

    companion object {

        fun emptyCoords() : C2Coords {
            return C2Coords(0,0)
        }

        fun createInitialCoords(p: C2Point) : C2Coords {
            val hp =  p.get(Dimension.H)
            val vp =  p.get(Dimension.V)
            return C2Coords(hp.minimumPosition,vp.minimumPosition)
        }

        private fun mergeCoords(b1: C2Coords, b2: C2Coords, d: Direction) : C2Coords {
            val out = when(d) {
                Direction.LEFT -> C2Coords(min(b1.h, b2.h), b2.v)
                Direction.RIGHT -> C2Coords(max(b1.h, b2.h), b2.v)
                Direction.UP -> C2Coords(b2.h, min(b1.v, b2.v))
                Direction.DOWN -> C2Coords(b2.h, max(b1.v, b2.v))
            }

            return out
        }

        fun createFollowingCoords(b: C2Coords, p: C2Point) : C2Coords {
            val nb = createInitialCoords(p)
            return mergeCoords(b, nb, p.d)
        }


    }
}