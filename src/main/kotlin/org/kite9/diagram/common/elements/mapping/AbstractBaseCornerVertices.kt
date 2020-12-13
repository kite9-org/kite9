package org.kite9.diagram.common.elements.mapping

import org.kite9.diagram.common.elements.vertex.MultiCornerVertex
import org.kite9.diagram.common.fraction.BigFraction
import org.kite9.diagram.common.fraction.BigFraction.Companion.ONE
import org.kite9.diagram.common.fraction.BigFraction.Companion.ZERO
import org.kite9.diagram.common.objects.OPair
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D
import java.util.*

abstract class AbstractBaseCornerVertices(
    rootContainer: DiagramElement,
    cx: OPair<BigFraction>,
    cy: OPair<BigFraction>,
    depth: Int
) : AbstractCornerVertices(
    rootContainer, cx, cy, depth
) {
    private val elements: MutableMap<OPair<BigFraction>, MultiCornerVertex> = HashMap()

    /**
     * There are no duplicates for independent container vertices.
     */
    override fun mergeDuplicates(cv: MultiCornerVertex, rh: RoutableHandler2D): MultiCornerVertex {
        return cv
    }

    override fun createVertex(x: BigFraction, y: BigFraction): MultiCornerVertex {
        return createVertexHere(x, y, elements)
    }

    override fun getTopContainerVertices(): AbstractCornerVertices {
        return this
    }

    override fun getAllAscendentVertices(): MutableCollection<MultiCornerVertex> {
        return elements.values.toMutableList()
    }

    override fun getVerticesAtThisLevel(): Collection<MultiCornerVertex> {
        return elements.values
    }

    override fun getAllDescendentVertices(): MutableCollection<MultiCornerVertex> {
        val out = super.getAllDescendentVertices()
        out.addAll(elements.values)
        return out
    }

    companion object {
        @JvmField
		val FULL_RANGE = OPair(ZERO, ONE)
    }
}