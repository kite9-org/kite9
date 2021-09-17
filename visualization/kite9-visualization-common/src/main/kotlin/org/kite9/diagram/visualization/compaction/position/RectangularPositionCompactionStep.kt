package org.kite9.diagram.visualization.compaction.position

import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.Port
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.position.BasicDimension2D
import org.kite9.diagram.model.position.CostedDimension2D
import org.kite9.diagram.model.position.Dimension2D
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.style.Measurement
import org.kite9.diagram.model.style.Placement
import org.kite9.diagram.visualization.compaction.AbstractCompactionStep
import org.kite9.diagram.visualization.compaction.Compaction
import org.kite9.diagram.visualization.compaction.Compactor
import org.kite9.diagram.visualization.compaction.Embedding
import org.kite9.diagram.visualization.display.CompleteDisplayer
import kotlin.math.max
import kotlin.math.min

class RectangularPositionCompactionStep(cd: CompleteDisplayer) : AbstractCompactionStep(cd) {

    override fun compact(c: Compaction, r: Embedding, rc: Compactor) {
        if (r.isTopEmbedding) {
            compactInternal(c)
        }
    }

    private fun compactInternal(c: Compaction) {
        val done: MutableSet<Rectangular> = HashSet()
        for (s in c.getHorizontalSegments()) {
            for ((de) in s.underlyingInfo) {
                if (de is Rectangular && !done.contains(de)) {
                    val r = de
                    done.add(r)
                    setRectanularRenderingInformation(r, c)
                }
            }
        }
    }

    private fun setRectanularRenderingInformation(r: Rectangular, c: Compaction) {
        val y = c.getHorizontalSegmentSlackOptimisation().getSlideablesFor(r)
        val x = c.getVerticalSegmentSlackOptimisation().getSlideablesFor(r)
        if (x != null && y != null) {
            val xMin = x.a!!.minimumPosition.toDouble()
            val xMax = x.b!!.minimumPosition.toDouble()
            val yMin = y.a!!.minimumPosition.toDouble()
            val yMax = y.b!!.minimumPosition.toDouble()
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
                        val pp = it.getPortPosition()
                        val direction = it.getPortDirection()
                        when (direction) {
                            Direction.LEFT -> setPortPosition(it, position.x(), measure(position.y(), size.y(), pp))
                            Direction.RIGHT -> setPortPosition(it, position.x()+size.x(), measure(position.y(), size.y(), pp))
                            Direction.UP -> setPortPosition(it, measure(position.x(), size.x(), pp), position.y())
                            Direction.DOWN -> setPortPosition(it, measure(position.x(), size.x(), pp), position.y()+size.y())
                        }
                    }
            }
        }
    }


    fun setPortPosition(p: Port, x: Double, y: Double) {

        val pos = p.getRenderingInformation().position
        if (pos == null) {
            val newPos = CostedDimension2D(x, y)
            p.getRenderingInformation().position = newPos
            p.getRenderingInformation().size = CostedDimension2D(0.0, 0.0)
        }
    }

    fun measure(start: Double, size: Double, pa: Placement) : Double {
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

    override val prefix: String
        get() = "RPCS"
}