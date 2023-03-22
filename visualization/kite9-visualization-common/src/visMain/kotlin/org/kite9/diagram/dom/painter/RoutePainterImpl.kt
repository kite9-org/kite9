package org.kite9.diagram.dom.painter

import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.RouteRenderingInformation
import kotlin.math.abs
import kotlin.math.min

/**
 * This class knows how to render anything with a
 * [RouteRenderingInformation] item. i.e. Context edges and links. It
 * provides functionality for handling rounded corners.
 *
 * @author robmoffat
 */
class RoutePainterImpl {

    interface EndDisplayer {
        /**
         * Call this method before draw to set the position of the EndDisplayer
         */
        fun reserve(m: Move, start: Boolean)
    }

    class ReservedLengthEndDisplayer(private val toReserve: Double) : EndDisplayer {
        override fun reserve(m: Move, start: Boolean) {
            if (start) {
                if (m.xs == m.xe) {
                    if (m.ys < m.ye) {
                        m.ys += toReserve.toFloat()
                    } else if (m.ys > m.ye) {
                        m.ys -= toReserve.toFloat()
                    } else {
                        throw LogicException()
                    }
                } else if (m.ys == m.ye) {
                    if (m.xs < m.xe) {
                        m.xs += toReserve.toFloat()
                    } else if (m.xs > m.xe) {
                        m.xs -= toReserve.toFloat()
                    } else {
                        throw LogicException()
                    }
                } else {
                    throw LogicException()
                }
            } else {
                if (m.xs == m.xe) {
                    if (m.ys < m.ye) {
                        m.ye -= toReserve.toFloat()
                    } else if (m.ys > m.ye) {
                        m.ye += toReserve.toFloat()
                    } else {
                        throw LogicException()
                    }
                } else if (m.ys == m.ye) {
                    if (m.xs < m.xe) {
                        m.xe -= toReserve.toFloat()
                    } else if (m.xs > m.xe) {
                        m.xe += toReserve.toFloat()
                    } else {
                        throw LogicException()
                    }
                } else {
                    throw LogicException()
                }
            }
        }
    }

    interface LineDisplayer {
        fun drawMove(m1: Move, next: Move?, prev: Move?, gp: Route)
        fun cornerRadius(): Float
    }

    val NULL_END_DISPLAYER: EndDisplayer = object : EndDisplayer {
        override fun reserve(m: Move, start: Boolean) {}
    }

    class CurvedCornerHopDisplayer(radius: Float) : AbstractCurveCornerDisplayer(radius) {
        override fun drawMove(m1: Move, next: Move?, prev: Move?, gp: Route) {
            if (m1.hopStart) {
                val c = m1.copy()
                c.trim(hopSize, 0f)
                drawHopStart(m1.xs.toDouble(), m1.ys.toDouble(), m1.xe.toDouble(), m1.ye.toDouble(), gp)
            }

            // draws a section of the line, plus bend into next one.
            if (m1.hopEnd) {
                val c = m1.copy()
                c.trim(0f, hopSize)
                gp.lineTo(c.xe.toDouble(), c.ye.toDouble())
                drawHopEnd(m1.xe.toDouble(), m1.ye.toDouble(), m1.xs.toDouble(), m1.ys.toDouble(), gp)
            } else if (next != null) {
                val c = m1.copy()
                val aRadius : Float = min(radius, m1.length / 2.0f)
                val bRadius : Float = min(radius, next.length / 2.0f)
                c.trim(0f, aRadius)
                gp.lineTo(c.xe.toDouble(), c.ye.toDouble())
                drawCorner(gp, m1, m1.direction, next.direction, aRadius.toDouble(), bRadius.toDouble())
            } else {
                gp.lineTo(m1.xe.toDouble(), m1.ye.toDouble())
            }
        }

        var hopSize = 15f

        fun drawHopStart(x1: Double, y1: Double, x2: Double, y2: Double, gp: Route): Double {
            if (x1 < x2) {
                // hop left
                gp.arc(x1 - hopSize, y1 - hopSize, (hopSize * 2).toDouble(), (hopSize * 2).toDouble(), false)
            } else if (x1 > x2) {
                // hop right
                gp.arc(x1 - hopSize, y1 - hopSize, (hopSize * 2).toDouble(), (hopSize * 2).toDouble(), true)
            } else {
                return 0.0
            }
            return hopSize.toDouble()
        }

        fun drawHopEnd(x1: Double, y1: Double, x2: Double, y2: Double, gp: Route): Double {
            if (x1 < x2) {
                // hop left
                gp.arc(x1 - hopSize, y1 - hopSize, (hopSize * 2).toDouble(), (hopSize * 2).toDouble(), false)
            } else if (x1 > x2) {

                // hop right
                gp.arc(x1 - hopSize, y1 - hopSize, (hopSize * 2).toDouble(), (hopSize * 2).toDouble(), true)

            } else {
                return 0.0
            }
            return hopSize.toDouble()
        }
    }

    abstract class AbstractCurveCornerDisplayer(var radius: Float) : LineDisplayer {
        override fun cornerRadius(): Float {
            return radius
        }

        protected fun drawCorner(gp: Route, a: Move, da: Direction, db: Direction, aRadius: Double, bRadius: Double) {
            if (radius == 0f) {
                return
            }
            when (da) {
                Direction.RIGHT -> if (db === Direction.UP) {
                    gp.arc(a.xe - aRadius, a.ye.toDouble() ,a.xe.toDouble(), a.ye-bRadius, false)
                } else if (db === Direction.DOWN) {
                    gp.arc(a.xe - aRadius, a.ye.toDouble(), a.xe.toDouble(), a.ye+bRadius, true)
                }
                Direction.LEFT -> if (db === Direction.UP) {
                    gp.arc(a.xe + aRadius, a.ye.toDouble(),  a.xe.toDouble(), a.ye - bRadius, true)
                } else if (db === Direction.DOWN) {
                    gp.arc(a.xe + aRadius, a.ye.toDouble(), a.xe.toDouble(), a.ye + bRadius, false)
                }
                Direction.UP -> if (db === Direction.LEFT) {
                    gp.arc(a.xe.toDouble(), a.ye + aRadius, a.xe - bRadius, a.ye.toDouble(), false)
                } else if (db === Direction.RIGHT) {
                    gp.arc(a.xe.toDouble(), a.ye + aRadius, a.xe + bRadius, a.ye.toDouble(), true)
                }
                Direction.DOWN -> if (db === Direction.LEFT) {
                    gp.arc(a.xe.toDouble(), a.ye - aRadius, a.xe - bRadius, a.ye.toDouble(), true)
                } else if (db === Direction.RIGHT) {
                    gp.arc(a.xe.toDouble(), a.ye - aRadius, a.xe + bRadius, a.ye.toDouble(), false)
                }
            }
        }
    }

    /**
     * Draws the routing of the edge.
     */
    fun drawRouting(
        r: RouteRenderingInformation?,
        start: EndDisplayer, end: EndDisplayer, line: LineDisplayer, closed: Boolean
    ): String {
        if (r == null || r.routePositions!!.size == 0) {
            return ""
        }
        val moves = createMoves(r, closed)
        val gp: Route = SVGRoute()
        for (i in moves.indices) {
            val a = moves[i]
            val next = moves[(i + 1) % moves.size]
            val prev = moves[(i + moves.size - 1) % moves.size]
            if (i == 0) {
                // start
                if (!closed) {
                    start.reserve(a, true)
                    gp.moveTo(a.xs.toDouble(), a.ys.toDouble())
                } else {
                    gp.moveTo(a.xs.toDouble(), a.ys.toDouble())
                }
            }
            if (i == moves.size - 1) {
                // end
                if (!closed) {
                    end.reserve(a, false)
                }
            }
            if (closed) {
                // middle
                line.drawMove(a, next, prev, gp)
            } else {
                line.drawMove(a, if (i == moves.size - 1) null else next, if (i == 0) null else prev, gp)
            }
        }
        if (closed) {
            gp.closePath()
        }
        return gp.toString()
    }


    /**
     * Moves are straight-line sections within the route
     *
     * @author robmoffat
     */
    data class Move(var xs: Float, var ys: Float, var xe: Float, var ye: Float, val hs: Boolean, val he: Boolean) {

        val hopStart : Boolean
        val hopEnd : Boolean

        val length = abs(xe - xs) + abs(ye - ys)

        fun trim(start: Float, end: Float) {
            if (xs == xe) {
                if (ys > ye) {
                    ys -= start
                    ye += end
                    return
                } else if (ys < ye) {
                    ys += start
                    ye -= end
                    return
                }
            } else {
                // vertical start
                if (xs > xe) {
                    xs -= start
                    xe += end
                    return
                } else if (xs < xe) {
                    xs += start
                    xe -= end
                    return
                }
            }

            //throw new LogicException("Don't know what to do here");
        }

        val direction: Direction
            get() {
                if (xe > xs) {
                    return Direction.RIGHT
                } else if (xe < xs) {
                    return Direction.LEFT
                } else if (ye > ys) {
                    return Direction.DOWN
                } else if (ye < ys) {
                    return Direction.UP
                }
                throw LogicException("empty move")
            }

        override fun toString(): String {
            return "($xs,$ys-$xe,$ye)"
        }

        fun copy(): Move {
            return Move(xs, ys, xe, ye, hopStart, hopEnd)
        }

        init {
            if (direction === Direction.LEFT || direction === Direction.RIGHT) {
                this.hopStart = hs
                this.hopEnd = he
            } else {
                this.hopStart = false
                this.hopEnd = false
            }
        }
    }

    private fun createMoves(r: RouteRenderingInformation, closed: Boolean): List<Move> {
        var fx = (if (closed) r.getWaypoint(r.getLength() - 1)!!.w else r.getWaypoint(0)!!.w).toInt()
        var fy = (if (closed) r.getWaypoint(r.getLength() - 1)!!.h else r.getWaypoint(0)!!.h).toInt()
        var fh = if (closed) r.isHop(r.getLength() - 1) else r.isHop(0)
        val out = ArrayList<Move>()
        var a: Move? = null
        var i = if (closed) 0 else 1
        val end = if (closed) r.getLength() else r.getLength() - 1
        while (i <= end) {
            val xn = r.getWaypoint(i % r.getLength())!!.w.toInt()
            val yn = r.getWaypoint(i % r.getLength())!!.h.toInt()
            val hn = r.isHop(i % r.getLength())
            var b = Move(
                fx.toFloat(), fy.toFloat(), xn.toFloat(), yn.toFloat(), fh, hn
            )
            if (a != null) {
                // not the first stroke
                val da = a.direction
                val db = b.direction
                if (da === db && !b.hopStart) {
                    // route parts in the same direction are part of the same
                    // move
                    b = Move(a.xs, a.ys, b.xe, b.ye, a.hopStart, b.hopEnd)
                } else if (da == null) {
                    // don't bother adding a null a
                } else if (db == null) {
                    b = a
                } else {
                    out.add(a)
                }
            }
            a = b
            i++
            fx = xn
            fy = yn
            fh = hn
        }
        if (a != null) {
            out.add(a)
        }

        // check first and last directions
        val first = out[0]
        val last = out[out.size - 1]
        if (first.direction === last.direction && closed) {
            out.remove(first)
            out.remove(last)
            out.add(Move(last.xs, last.ys, first.xe, first.ye, first.hopEnd, last.hopStart))
        }
        if (!closed) {
            // trim the first and last moves so that they don't get occluded by the shape they are meeting
        }
        return out
    }
}