package org.kite9.diagram.common.elements.vertex

import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.Dimension2D
import org.kite9.diagram.model.position.HPos
import org.kite9.diagram.model.position.RectangleRenderingInformation
import org.kite9.diagram.model.position.VPos

/**
 * Provides the Anchor class, which can be used to set the position for a number of
 * [DiagramElement]s.
 * @author robmoffat
 */
abstract class AbstractAnchoringVertex(id: String) : AbstractVertex(id) {

    class Anchor(val ud: VPos?, val lr: HPos?, val de: DiagramElement) {

        private val rI: RectangleRenderingInformation
            private get() = de.getRenderingInformation() as RectangleRenderingInformation

        fun setX(x: Double) {
            try {
                if (de == null) {
                    return
                }
                val ri = rI
                var l: Double = if (ri.position == null) 0.0 else ri.position.x()
                var r = if (ri.size == null) l else l + ri.size.width
                val u: Double = if (ri.position == null) 0.0 else ri.position.y()
                val d = if (ri.size == null) u else u + ri.size.height
                if (lr == HPos.LEFT) {
                    l = x
                } else if (lr == HPos.RIGHT) {
                    r = x
                }
                ri.position = Dimension2D(l, u)
                ri.size = Dimension2D(r - l, d - u)
            } catch (e: NullPointerException) {
                throw LogicException("NPE setting position of $this", e)
            }
        }

        fun setY(y: Double) {
            try {
                if (de == null) {
                    return
                }
                val ri = rI
                val l: Double = if (ri.position == null) 0.0 else ri.position.x()
                val r = if (ri.size == null) l else l + ri.size.width
                var u: Double = if (ri.position == null) 0.0 else ri.position.y()
                var d = if (ri.size == null) u else u + ri.size.height
                if (ud == VPos.UP) {
                    u = y
                } else if (ud == VPos.DOWN) {
                    d = y
                }
                ri.position = Dimension2D(l, u)
                ri.size = Dimension2D(r - l, d - u)
            } catch (e: NullPointerException) {
                throw LogicException("NPE setting position of $this", e)
            }
        }

        override fun toString(): String {
            return de.toString() + "-" + lr + "-" + ud
        }
    }
}