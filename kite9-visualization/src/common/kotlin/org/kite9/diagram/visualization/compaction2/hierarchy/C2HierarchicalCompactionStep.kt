package org.kite9.diagram.visualization.compaction2.hierarchy

import org.kite9.diagram.visualization.compaction2.AbstractC2CompactionStep
import org.kite9.diagram.visualization.compaction2.C2Compaction
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.rhd.grouping.GroupResult
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group

class C2HierarchicalCompactionStep(val mr: GroupResult, cd: CompleteDisplayer) : AbstractC2CompactionStep(cd) {
    override fun compact(c: C2Compaction, g: Group) {
        innerCompact(c,mr.groups().first())
    }

    private fun innerCompact(c: C2Compaction, first: Group) {
        if (first is CompoundGroup) {
            innerCompact(c, first.a)
            innerCompact(c, first.b)


        }
    }

    override val prefix: String?
        get() = "HIER"

    override val isLoggingEnabled: Boolean
        get() = true

}