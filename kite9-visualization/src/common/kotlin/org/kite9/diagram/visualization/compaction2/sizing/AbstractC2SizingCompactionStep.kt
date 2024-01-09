package org.kite9.diagram.visualization.compaction2.sizing

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.visualization.compaction.AbstractCompactionStep
import org.kite9.diagram.visualization.compaction.Compaction
import org.kite9.diagram.visualization.compaction.Compactor
import org.kite9.diagram.visualization.compaction.Embedding
import org.kite9.diagram.visualization.compaction2.AbstractC2CompactionStep
import org.kite9.diagram.visualization.compaction2.C2Compaction
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group

abstract class AbstractC2SizingCompactionStep(cd: CompleteDisplayer) : AbstractC2CompactionStep(cd) {

    override fun compact(c: C2Compaction, g: Group) {
        size(c, Dimension.H)
        size(c, Dimension.V)
    }

    private fun size(c: C2Compaction, dimension: Dimension) {
        val so = c.getSlackOptimisation(dimension)
        so.getAllPositioned()
            .filterIsInstance<Rectangular>()
            .filter { filter(it, dimension) }
            .distinct()
            .sortedWith { a, b  -> compare(a, b, c, dimension) }
            .forEach{ performSizing(it, c, dimension) }
    }

    abstract fun filter(r: Rectangular, d: Dimension): Boolean
    abstract fun compare(a: Rectangular, b: Rectangular, c: C2Compaction, d: Dimension): Int
    abstract fun performSizing(r: Rectangular, c: C2Compaction, d: Dimension)
    override val isLoggingEnabled: Boolean
        get() = true
}