package org.kite9.diagram.visualization.compaction2.hierarchy

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Diagram
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.AbstractC2CompactionStep
import org.kite9.diagram.visualization.compaction2.C2Compaction
import org.kite9.diagram.visualization.compaction2.C2SlackOptimisation
import org.kite9.diagram.visualization.compaction2.C2Slideable
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import kotlin.math.max
import kotlin.math.min

class C2NeighbourBuilderStep(cd: CompleteDisplayer) : AbstractC2CompactionStep(cd) {

    override fun compact(c: C2Compaction, g: Group) {
        c.buildNeighbours()
    }



    override val prefix: String
        get() = "NBCS"

    override val isLoggingEnabled: Boolean
        get() = true

}