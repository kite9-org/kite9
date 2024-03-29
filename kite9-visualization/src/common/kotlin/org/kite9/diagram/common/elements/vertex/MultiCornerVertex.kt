package org.kite9.diagram.common.elements.vertex

import org.kite9.diagram.common.fraction.LongFraction
import org.kite9.diagram.common.fraction.LongFraction.Companion.ONE
import org.kite9.diagram.common.fraction.LongFraction.Companion.ONE_HALF
import org.kite9.diagram.common.fraction.LongFraction.Companion.ZERO
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.HPos
import org.kite9.diagram.model.position.VPos

/**
 * Represents corners of diagrams, containers and any other rectangular content.
 * For elements with grid-layout, these also represent points within the grid that will need to be connected up.
 * Multi-corners can be the corners of multiple different diagram elements.
 */
open class MultiCornerVertex(id: String, val xOrdinal: LongFraction, val yOrdinal: LongFraction) :
    AbstractAnchoringVertex(id + "_" + xOrdinal + "_" + yOrdinal), MultiElementVertex {

    override fun hasDimension(): Boolean {
        return false
    }

    private val anchors: MutableSet<Anchor> = mutableSetOf()

    fun getAnchors(): Set<Anchor> {
        return anchors
    }

    fun addAnchor(lr: HPos?, ud: VPos?, underlying: DiagramElement) {
        anchors.add(Anchor(ud, lr, underlying))
    }

    fun hasAnchorFor(c: DiagramElement): Boolean {
        for (anchor in anchors) {
            if (anchor.de === c) {
                return true
            }
        }
        return false
    }

    override fun isPartOf(c: DiagramElement?): Boolean {
        return if (c == null) false else hasAnchorFor(c)
    }

    override fun getDiagramElements(): Set<DiagramElement> {
        return anchors
            .map { a: Anchor -> a.de }
            .toSet()
    }

    companion object {

		fun isMin(b: LongFraction): Boolean {
            return b.equals(ZERO)
        }

		fun isMax(b: LongFraction): Boolean {
            return b.equals(ONE)
        }

		fun getOrdForXDirection(d: Direction?): LongFraction {
            return when (d) {
                Direction.LEFT -> ZERO
                Direction.RIGHT -> ONE
                else -> ONE_HALF
            }
        }

		fun getOrdForYDirection(d: Direction?): LongFraction {
            return when (d) {
                Direction.UP -> ZERO
                Direction.DOWN -> ONE
                else -> ONE_HALF
            }
        }
    }

}