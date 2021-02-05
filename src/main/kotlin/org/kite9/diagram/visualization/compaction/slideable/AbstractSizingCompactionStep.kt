package org.kite9.diagram.visualization.compaction.slideable

import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.visualization.compaction.AbstractCompactionStep
import org.kite9.diagram.visualization.compaction.Compaction
import org.kite9.diagram.visualization.compaction.Compactor
import org.kite9.diagram.visualization.compaction.Embedding
import org.kite9.diagram.visualization.display.CompleteDisplayer

abstract class AbstractSizingCompactionStep(cd: CompleteDisplayer) : AbstractCompactionStep(cd) {

    override fun compact(c: Compaction, e: Embedding, rc: Compactor) {
        if (e.isTopEmbedding) {
            size(c, true)
            size(c, false)
        }
    }

    private fun size(c: Compaction, horizontal: Boolean) {
        c.getHorizontalSegments()
            .flatMap { it.underlyingInfo }
            .map { it.diagramElement }
            .filterIsInstance<Rectangular>()
            .filter { filter(it, horizontal) }
            .distinct()
            .sortedWith { a, b  -> compare(a, b, c, horizontal) }
            .forEach{ performSizing(it, c, horizontal) }
    }

    abstract fun filter(r: Rectangular, horizontal: Boolean): Boolean
    abstract fun compare(a: Rectangular, b: Rectangular, c: Compaction, horizontal: Boolean): Int
    abstract fun performSizing(r: Rectangular, c: Compaction, horizontal: Boolean)
}