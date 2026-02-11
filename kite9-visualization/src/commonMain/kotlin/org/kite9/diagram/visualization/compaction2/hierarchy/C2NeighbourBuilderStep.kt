package org.kite9.diagram.visualization.compaction2.hierarchy

import org.kite9.diagram.visualization.compaction2.AbstractC2CompactionStep
import org.kite9.diagram.visualization.compaction2.C2Compaction
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group

class C2NeighbourBuilderStep(cd: CompleteDisplayer) : AbstractC2CompactionStep(cd) {

    override fun compact(c: C2Compaction, g: Group) {
        //c.buildNeighbours()
        c.joinOverlappingNeighbourGroups()
    }



    override val prefix: String
        get() = "NBCS"

    override val isLoggingEnabled: Boolean
        get() = true

}