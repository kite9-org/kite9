package org.kite9.diagram.visualization.compaction2.hierarchy

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.*
import org.kite9.diagram.model.position.BasicDimension2D
import org.kite9.diagram.model.position.CostedDimension2D
import org.kite9.diagram.model.position.Dimension2D
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.style.Measurement
import org.kite9.diagram.model.style.Placement
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.*
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import kotlin.math.max
import kotlin.math.min

class C2RectangularPositionCompactionStep(cd: CompleteDisplayer) : AbstractC2CompactionStep(cd) {

    override fun compact(c: C2Compaction, g: Group) {
        visit(c.getDiagram(), c)
    }

    private fun visit(r: Rectangular, c: C2Compaction) {
        log.send("Positioning $r")
        val ssy = c.getSlackOptimisation(Dimension.H).getSlideablesFor(r)
        val ssx = c.getSlackOptimisation(Dimension.V).getSlideablesFor(r)
        if ((ssy != null) && (ssx != null)) {
            val xMin = getSlideable(r, Side.START, ssy)!!.minimumPosition.toDouble()
            val xMax = getSlideable(r, Side.END, ssy)!!.minimumPosition.toDouble()
            val yMin = getSlideable(r, Side.START, ssx)!!.minimumPosition.toDouble()
            val yMax =  getSlideable(r, Side.END, ssx)!!.minimumPosition.toDouble()
            val rri = r.getRenderingInformation()
            val position = BasicDimension2D(xMin, yMin)
            rri.position = position
            val size: Dimension2D = BasicDimension2D(xMax - xMin, yMax - yMin)
            if (size.w < 0 || size.h < 0) {
                throw LogicException("Slideable issue")
            }
            rri.size = size

            if (r is Container) {
                // handle any ports that are visible too
                r.getContents()
                    .filterIsInstance<Port>()
                    .forEach {
                        val pp = it.getContainerPosition()
                        val direction = it.getPortDirection()
                        when (direction) {
                            Direction.LEFT -> setPortPosition(it, position.x(), measure(position.y(), size.y(), pp))
                            Direction.RIGHT -> setPortPosition(
                                it,
                                position.x() + size.x(),
                                measure(position.y(), size.y(), pp)
                            )

                            Direction.UP -> setPortPosition(it, measure(position.x(), size.x(), pp), position.y())
                            Direction.DOWN -> setPortPosition(
                                it,
                                measure(position.x(), size.x(), pp),
                                position.y() + size.y()
                            )
                        }
                    }
            }
        }

        if (r is Container) {
            r.getContents()
                .filterIsInstance<Rectangular>()
                .forEach { visit(it, c) }

            r.getContents()
                .filterIsInstance<Connection>()
                .forEach { conn ->
                    conn.getFromLabel()?.let { visit(it, c) }
                    conn.getToLabel()?.let { visit(it, c) }
                }
        }
    }


    private fun setPortPosition(p: Port, x: Double, y: Double) {
        val pos = p.getRenderingInformation().position
        if (pos == null) {
            val newPos = CostedDimension2D(x, y)
            p.getRenderingInformation().position = newPos
            p.getRenderingInformation().size = CostedDimension2D(0.0, 0.0)
        }
    }

    private fun measure(start: Double, size: Double, pa: Placement) : Double {
        val unbounded = when (pa.type) {
            Measurement.PERCENTAGE -> start + (pa.amount / 100.0) * size
            else -> if (pa.amount > 0) {
                start + pa.amount
            } else {
                // measured from the end
                return start + size + pa.amount
            }
        }

        val bounded = max(start, min(unbounded, start+size))
        return bounded
    }

    private fun getSlideable(r: DiagramElement, s: Side, set: RectangularSlideableSet) : C2Slideable? {
        return set.getRectangularSlideables()
            .filter { x->
                x.anchors
                    .filter { it.e == r }
                    .firstOrNull { it.s == s } != null
            }.firstOrNull()
    }

    override val prefix: String
        get() = "RPCS"

    override val isLoggingEnabled: Boolean
        get() = true
}