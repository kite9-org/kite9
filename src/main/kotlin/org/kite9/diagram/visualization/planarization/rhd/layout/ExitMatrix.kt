package org.kite9.diagram.visualization.planarization.rhd.layout

import org.kite9.diagram.common.elements.RoutingInfo
import org.kite9.diagram.common.objects.BasicBounds
import org.kite9.diagram.common.objects.Bounds
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkDetail
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D.DPos
import java.lang.StringBuilder

/**
 * The exit matrix keeps track of links leaving a group in order that we can perform
 *
 * @author robmoffat
 */
class ExitMatrix {
    enum class RelativeSide {
        FACING, MIDDLE, OPPOSITE
    }

    private var counts = arrayOfNulls<FloatArray>(3)

    private var spans = arrayOf<Bounds>(
        BasicBounds.EMPTY_BOUNDS,
        BasicBounds.EMPTY_BOUNDS,
        BasicBounds.EMPTY_BOUNDS,
        BasicBounds.EMPTY_BOUNDS
    )

    private var sizex: Bounds = BasicBounds.EMPTY_BOUNDS
    private var sizey: Bounds = BasicBounds.EMPTY_BOUNDS

    var isEmpty = true
        private set

    private fun getCount(x: Int, y: Int): Float {
        return counts[y]!![x]
    }

    private fun incrCount(x: DPos, y: DPos, v: Float) {
        counts[getIndex(y)]!![getIndex(x)] += v
    }

    private fun getIndex(d: DPos): Int {
        return d.ordinal
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("ExitMatrix[spans=");
        spans.forEach {
            sb.append(it)
            sb.append(" ")
        }
        outputArray(sb, counts[0]!!)
        outputArray(sb, counts[1]!!)
        outputArray(sb, counts[2]!!)
        sb.append("]")
        return sb.toString()
    }

    private fun outputArray(sb: StringBuilder, a: FloatArray) {
        a.forEach {
            sb.append(it)
            sb.append(" ")
        }
        sb.append(" / ")
    }

    constructor() {
        for (i in counts.indices) {
            counts[i] = FloatArray(3)
        }
    }

    constructor(counts: Array<FloatArray?>, empty: Boolean) {
        this.counts = counts
        isEmpty = empty
    }

    fun setSpans(spans: Array<Bounds>) {
        this.spans = spans
    }

    fun setSize(x: Bounds, y: Bounds) {
        sizex = x
        sizey = y
    }

    fun addLink(
        originatingGroup: Group,
        destinationGroup: Group,
        ld: LinkDetail,
        rh: RoutableHandler2D
    ) {
        //System.out.println("Adding link from "+originatingGroup+ " to "+destinationGroup);
        val oPos = originatingGroup.axis.getPosition(rh, true)
        val dPos = destinationGroup.axis.getPosition(rh, true)
        val xCompare = rh.compareBounds(rh.getBoundsOf(dPos, true), rh.getBoundsOf(oPos, true))
        val yCompare = rh.compareBounds(rh.getBoundsOf(dPos, false), rh.getBoundsOf(oPos, false))
        incrCount(xCompare, yCompare, ld.numberOfLinks)
        if (yCompare === DPos.OVERLAP) {
            if (xCompare === DPos.BEFORE) {
                expandBounds(Direction.LEFT, oPos, rh)
            } else if (xCompare === DPos.AFTER) {
                expandBounds(Direction.RIGHT, oPos, rh)
            }
        }
        if (xCompare === DPos.OVERLAP) {
            if (yCompare === DPos.BEFORE) {
                expandBounds(Direction.UP, oPos, rh)
            } else if (yCompare === DPos.AFTER) {
                expandBounds(Direction.DOWN, oPos, rh)
            }
        }
        isEmpty = false
        //System.out.println(this);
    }

    private fun expandBounds(d: Direction, oPos: RoutingInfo, rh: RoutableHandler2D) {
        val b: Bounds = when (d) {
            Direction.LEFT, Direction.RIGHT -> rh.getBoundsOf(
                oPos,
                false
            )
            Direction.UP, Direction.DOWN -> rh.getBoundsOf(
                oPos,
                true
            )
        }
        spans[d.ordinal] = spans[d.ordinal].expand(b)
    }

    fun getLinkCount(d: Layout?, rs: RelativeSide, rank: Int): Float {
        val rsRank = rs.ordinal
        return when (d) {
            Layout.UP -> getCount(rank + 1, rsRank)
            Layout.DOWN -> getCount(rank + 1, 2 - rsRank)
            Layout.LEFT -> getCount(rsRank, rank + 1)
            Layout.RIGHT -> getCount(2 - rsRank, rank + 1)
            else -> throw LogicException("Not sure what to return")
        }
    }

    fun getSpanInDirectionOfLayout(d: Layout): Bounds? {
        return when (d) {
            Layout.UP -> spans[Direction.UP.ordinal]
            Layout.DOWN -> spans[Direction.DOWN.ordinal]
            Layout.LEFT -> spans[Direction.LEFT.ordinal]
            Layout.RIGHT -> spans[Direction.RIGHT.ordinal]
            else -> throw LogicException("Span not defined for $d")
        }
    }

    fun getSizeInDirectionOfLayout(d: Layout?): Bounds {
        return when (d) {
            Layout.DOWN, Layout.UP, Layout.VERTICAL -> sizey
            else -> sizex
        }
    }
}