package org.kite9.diagram.visualization.compaction.rect

import org.kite9.diagram.visualization.compaction.AbstractCompactionStep
import org.kite9.diagram.visualization.compaction.Compaction
import org.kite9.diagram.visualization.compaction.Compactor
import org.kite9.diagram.visualization.compaction.Embedding
import org.kite9.diagram.visualization.display.CompleteDisplayer

/**
 * This makes sure that compaction proceeds bottom-up through the diagram.
 *
 * @author robmoffat
 */
class HierarchicalCompactionStep(cd: CompleteDisplayer) : AbstractCompactionStep(cd) {

    override fun compact(c: Compaction, e: Embedding, rc: Compactor) {
        log.send("Compacting: $e")
        for (e2 in e.innerEmbeddings) {
            rc.compact(e2, c)
        }
    }

    override val prefix: String
        get() = "HCS "
}