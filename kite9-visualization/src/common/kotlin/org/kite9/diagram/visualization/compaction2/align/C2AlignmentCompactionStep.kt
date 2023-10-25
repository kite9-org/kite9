package org.kite9.diagram.visualization.compaction2.align

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.model.*
import org.kite9.diagram.visualization.compaction2.AbstractC2CompactionStep
import org.kite9.diagram.visualization.compaction2.C2Compaction
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group

/**
 * At the moment, this passes through each aligner in turn, from top-to-bottom of the diagram.
 */
class C2AlignmentCompactionStep(cd: CompleteDisplayer, private val aligners: Array<Aligner>) : AbstractC2CompactionStep(cd) {

    override val prefix: String
        get() = "ALN "

    override val isLoggingEnabled: Boolean
        get() = true

    override fun compact(c: C2Compaction, g: Group) {
        alignContents(c.getDiagram(), c, Dimension.H)
        alignContents(c.getDiagram(), c, Dimension.V)
    }

    private fun alignContents(de: Container, c: C2Compaction, d: Dimension) {
        val contents: List<DiagramElement> = de.getContents()
        for (a in aligners) {
            alignConnecteds(c, contents, a, d, de)
            alignLabels(c, contents, a, d, de)
        }
        for (de2 in contents) {
            if (de2 is Container) {
                alignContents(de2, c, d)
            }
        }
    }

    private fun alignConnecteds(c: C2Compaction, contents: List<DiagramElement>, a: Aligner, d: Dimension, de: Container) {
        val filtered = contents
            .filterIsInstance<Connected>()
            .filterIsInstance<Rectangular>()
            .filter { a.willAlign(it, d) }
            .toSet()
        if (filtered.isNotEmpty()) {
            a.alignFor(de, filtered, c, d)
        }
    }

    private fun alignLabels(c: C2Compaction, contents: List<DiagramElement>, a: Aligner, d: Dimension, de: Container) {
        val filtered = contents
            .filterIsInstance<Label>()
            .filter { a.willAlign(it, d) }
            .toSet()
        if (filtered.isNotEmpty()) {
            a.alignFor(de, filtered, c, d)
        }
    }


}