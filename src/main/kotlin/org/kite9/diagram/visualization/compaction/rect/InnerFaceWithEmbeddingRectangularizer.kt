package org.kite9.diagram.visualization.compaction.rect

import org.kite9.diagram.visualization.compaction.Compaction
import org.kite9.diagram.visualization.compaction.Compactor
import org.kite9.diagram.visualization.compaction.Embedding
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.orthogonalization.DartFace

/**
 * First-pass rectangularization only does square faces with content in them.
 *
 * @author robmoffat
 */
class InnerFaceWithEmbeddingRectangularizer(cd: CompleteDisplayer) : AbstractRectangularizer(cd) {

    override fun compact(c: Compaction, r: Embedding, rc: Compactor) {
        log.send("InnerFaceWithEmbeddingRectangularizer Of: $r")
        super.compact(c, r, rc)
    }

    override fun selectFacesToRectangularize(c: Compaction, faces: List<DartFace>): List<DartFace> {
        return faces
            .filter { it.containedFaces.size > 0 }
    }

    /**
     * This version will remove any element from the map where there are more than 4 turns (i.e.
     * it's not an initial rectangle anyway).
     */
    override fun performFaceRectangularization(c: Compaction, stacks: MutableMap<DartFace, MutableList<VertexTurn>>) {
        val iterator = stacks.entries.iterator()
        while (iterator.hasNext()) {
            val elem = iterator.next()
            if (elem.value.size > 4) {
                iterator.remove()
            }
        }
    }
}