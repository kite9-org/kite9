package org.kite9.diagram.visualization.compaction.slideable

import org.kite9.diagram.visualization.compaction.Embedding.isTopEmbedding
import org.kite9.diagram.visualization.compaction.Compaction.getHorizontalSegments
import org.kite9.diagram.visualization.compaction.segment.Segment.underlyingInfo
import org.kite9.diagram.visualization.compaction.segment.UnderlyingInfo.diagramElement
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.compaction.AbstractCompactionStep
import org.kite9.diagram.visualization.compaction.Compaction
import org.kite9.diagram.visualization.compaction.Embedding
import org.kite9.diagram.visualization.compaction.Compactor
import org.kite9.diagram.visualization.compaction.segment.UnderlyingInfo
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.visualization.compaction.segment.Segment
import java.util.Comparator
import java.util.function.Consumer
import java.util.function.Predicate

abstract class AbstractSizingCompactionStep(cd: CompleteDisplayer?) : AbstractCompactionStep(cd!!) {
    override fun compact(c: Compaction, e: Embedding, rc: Compactor) {
        if (e.isTopEmbedding) {
            size(c, true)
            size(c, false)
        }
    }

    private fun size(c: Compaction, horizontal: Boolean) {
        c.getHorizontalSegments().stream()
            .flatMap { s: Segment -> s.underlyingInfo.stream() }
            .map { (diagramElement) -> diagramElement }
            .filter { de: DiagramElement? -> de is Rectangular }
            .map { de: DiagramElement? -> de as Rectangular? }
            .filter(Predicate { r: Rectangular? -> filter(r, horizontal) })
            .distinct()
            .sorted(Comparator { a: Rectangular?, b: Rectangular? -> compare(a, b, c, horizontal) })
            .forEach(Consumer { r: Rectangular? -> performSizing(r, c, horizontal) })
    }

    abstract fun filter(r: Rectangular?, horizontal: Boolean): Boolean
    abstract fun compare(a: Rectangular?, b: Rectangular?, c: Compaction?, horizontal: Boolean): Int
    abstract fun performSizing(r: Rectangular?, c: Compaction?, horizontal: Boolean)
}