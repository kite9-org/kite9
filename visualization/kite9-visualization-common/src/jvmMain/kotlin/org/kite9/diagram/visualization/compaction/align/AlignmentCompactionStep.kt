package org.kite9.diagram.visualization.compaction.align

import org.kite9.diagram.model.*
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
            alignConnecteds(c, contents, a, true, de)
            alignConnecteds(c, contents, a, false, de)
            alignLabels(c, contents, a, true, de)
            alignLabels(c, contents, a, false, de)
        }
        for (de2 in contents) {
            if (de2 is Container) {
                alignContents(de2, c)
            }
        }
    }

    private fun alignConnecteds(c: Compaction, contents: List<DiagramElement>, a: Aligner, horizontal: Boolean, de: Container) {
        val filtered = contents
            .filterIsInstance<Connected>()
            .filterIsInstance<Rectangular>()
            .filter { a.willAlign(it, horizontal) }
            .toSet()
        if (filtered.isNotEmpty()) {
            a.alignFor(de, filtered, c, horizontal)
        }
    }

    private fun alignLabels(c: Compaction, contents: List<DiagramElement>, a: Aligner, horizontal: Boolean, de: Container) {
        val filtered = contents
            .filterIsInstance<Label>()
            .filterIsInstance<Rectangular>()
            .filter { a.willAlign(it, horizontal) }
            .toSet()
        if (filtered.isNotEmpty()) {
            a.alignFor(de, filtered, c, horizontal)
        }
    }


}