package org.kite9.diagram.visualization.compaction.align

import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.visualization.compaction.AbstractCompactionStep
import org.kite9.diagram.visualization.compaction.Compaction
import org.kite9.diagram.visualization.compaction.Compactor
import org.kite9.diagram.visualization.compaction.Embedding
import org.kite9.diagram.visualization.display.CompleteDisplayer
/**
 * At the moment, this passes through each aligner in turn, from top-to-bottom of the diagram.
 */
class AlignmentCompactionStep(cd: CompleteDisplayer, vararg aligners: Aligner) : AbstractCompactionStep(cd) {
    var aligners: Array<Aligner> = aligners as Array<Aligner>

    override val prefix: String
        get() = "ALN "

    override fun compact(c: Compaction, e: Embedding, rc: Compactor) {
        if (e.isTopEmbedding) {
            alignContents(c.getHorizontalSegmentSlackOptimisation().theDiagram, c)
        }
    }

    protected fun alignContents(de: Container, c: Compaction) {
        val contents: List<DiagramElement> = de.getContents()
        for (a in aligners) {
            alignOnAxis(c, contents, a, true, de)
            alignOnAxis(c, contents, a, false, de)
        }
        for (de2 in contents) {
            if (de2 is Container) {
                alignContents(de2, c)
            }
        }
    }

    private fun alignOnAxis(c: Compaction, contents: List<DiagramElement>, a: Aligner, horizontal: Boolean, de: Container) {
        val filtered = contents
            .filterIsInstance<Rectangular>()
            .filter { a.willAlign(it, horizontal) }
            .filterIsInstance<Connected>()
            .toSet()
        if (filtered.isNotEmpty()) {
            a.alignFor(de, filtered, c, horizontal)
        }
    }

}