package org.kite9.diagram.visualization.compaction.rect

import org.kite9.diagram.visualization.compaction.Compaction
import org.kite9.diagram.visualization.compaction.Compactor
import org.kite9.diagram.visualization.compaction.Embedding
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.orthogonalization.DartFace

open class NonEmbeddedFaceRectangularizer(cd: CompleteDisplayer) : MidSideCheckingRectangularizer(cd) {

    override fun compact(c: Compaction, r: Embedding, rc: Compactor) {
        log.send("NonEmbeddedFaceRectangularizer: $r")
        super.compact(c, r, rc)
    }

    override fun selectFacesToRectangularize(c: Compaction, faces: List<DartFace>): List<DartFace> {
        return faces
            .filter {
                val r = c.getFaceSpace(it)
                r == null
            }
    }
}