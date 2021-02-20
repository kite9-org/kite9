package org.kite9.diagram.visualization.compaction.position

import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.position.BasicDimension2D
import org.kite9.diagram.model.position.Dimension2D
import org.kite9.diagram.visualization.compaction.AbstractCompactionStep
import org.kite9.diagram.visualization.compaction.Compaction
import org.kite9.diagram.visualization.compaction.Compactor
import org.kite9.diagram.visualization.compaction.Embedding
import org.kite9.diagram.visualization.display.CompleteDisplayer

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
            rri.position = BasicDimension2D(xMin, yMin)
            val size: Dimension2D = BasicDimension2D(xMax - xMin, yMax - yMin)
            if (size.w < 0 || size.h < 0) {
                throw LogicException("Slideable issue")
            }
            rri.size = size
        }
    }

    override val prefix: String
        get() = "RPCS"
}