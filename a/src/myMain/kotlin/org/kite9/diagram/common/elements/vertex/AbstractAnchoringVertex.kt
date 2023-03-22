package org.kite9.diagram.common.elements.vertex

import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.HPos
import org.kite9.diagram.model.position.VPos

/**
 * Provides the Anchor class, which can be used to set the position for a number of
 * [DiagramElement]s.
 * @author robmoffat
 */
abstract class AbstractAnchoringVertex(id: String) : AbstractVertex(id) {

    data class Anchor(val ud: VPos?, val lr: HPos?, val de: DiagramElement) {

        override fun toString(): String {
            return "$de-$lr-$ud"
        }
    }
}