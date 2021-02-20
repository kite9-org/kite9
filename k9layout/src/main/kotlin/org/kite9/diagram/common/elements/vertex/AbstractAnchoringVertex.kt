package org.kite9.diagram.common.elements.vertex

import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.CostedDimension2D
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
                val position = ri.position
                val size = ri.size

                var l: Double = if (position == null) 0.0 else position.x()
                var r = if (size == null) l else l + size.width()
                val u: Double = if (position == null) 0.0 else position.y()
                val d = if (size == null) u else u + size.height()

                if (lr == HPos.LEFT) {
                    l = x
                } else if (lr == HPos.RIGHT) {
                    r = x
                }
                ri.position = CostedDimension2D(l, u)
                ri.size = CostedDimension2D(r - l, d - u)
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
                val position = ri.position
                val size = ri.size

                val l: Double = if (position == null) 0.0 else position.x()
                val r = if (size == null) l else l + size.width()
                var u: Double = if (position == null) 0.0 else position.y()
                var d = if (size == null) u else u + size.height()

                if (ud == VPos.UP) {
                    u = y

                } else if (ud == VPos.DOWN) {
                    d = y
                }
                ri.position = CostedDimension2D(l, u)
                ri.size = CostedDimension2D(r - l, d - u)
            } catch (e: NullPointerException) {
                throw LogicException("NPE setting position of $this", e)
            }
        }

        override fun toString(): String {
            return de.toString() + "-" + lr + "-" + ud
        }
    }
}