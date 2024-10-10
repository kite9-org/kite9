package org.kite9.diagram.visualization.compaction2.sizing

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Diagram
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.*
import org.kite9.diagram.visualization.compaction2.anchors.RectAnchor
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group

/**
 * Ensures that the overall diagram width is minimal.
 * @author robmoffat
 */
class C2DiagramSizeCompactionStep(cd: CompleteDisplayer) : AbstractC2CompactionStep(cd) {

    override fun compact(c: C2Compaction, g: Group) {
        setFor(c.getSlackOptimisation(Dimension.H), c.getDiagram())
        setFor(c.getSlackOptimisation(Dimension.V), c.getDiagram())
    }

    private fun setFor(o: C2SlackOptimisation, diagram: Diagram) {
        val set = o.getSlideablesFor(diagram)
        val highSide = set?.getRectangularSlideables()?.firstOrNull { it.getRectangulars().contains(RectAnchor(diagram, Side.END)) }

        if (highSide != null) {
            val min = highSide.minimumPosition
            highSide.maximumPosition = min
            log.send("Set Overall Diagram Size: $highSide")
        } else {
            throw LogicException("Couldn't find rect for diagram")
        }
    }

    override val prefix: String
        get() = "DSCS"

    override val isLoggingEnabled: Boolean
        get() = true
}