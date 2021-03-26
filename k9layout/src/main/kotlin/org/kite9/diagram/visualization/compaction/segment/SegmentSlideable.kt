package org.kite9.diagram.visualization.compaction.segment

import org.kite9.diagram.common.BiDirectional
import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction.UnderlyingInfo
import org.kite9.diagram.visualization.compaction.slideable.ElementSlideable
import org.kite9.diagram.visualization.orthogonalization.Dart

class SegmentSlideable(
    so: SegmentSlackOptimisation,
    dimension: Dimension,
    number: Int,
    override val verticesOnSlideable: Set<Vertex>
) : ElementSlideable(so, dimension, number) {

    override fun toString(): String {
        return "<$identifier $verticesOnSlideable ${minimum.position},${maximum.position}>"
    }



    override val underlyingInfo: Set<UnderlyingInfo> by lazy {
        dartsInSegment
            .flatMap { convertUnderlyingToUnderlyingInfo(it) }
            .filter { a: UnderlyingInfo -> a.diagramElement != null }
            .toSet()
    }

    /**
     * This is a utility method, used to set the positions of the darts for the diagram
     */
    val dartsInSegment: Collection<Dart> by lazy {
        verticesOnSlideable
            .flatMap { it.getEdges() }
            .filterIsInstance<Dart>()
            .filter {
                if (dimension === Dimension.H) {
                    (it.getDrawDirection() === Direction.LEFT || it.getDrawDirection() === Direction.RIGHT)
                } else {
                    (it.getDrawDirection() === Direction.UP || it.getDrawDirection() === Direction.DOWN)
                }
            }
    }


    private fun convertUnderlyingToUnderlyingInfo(d: Dart): Iterable<UnderlyingInfo> {
        val diagramElements: Map<DiagramElement, Direction?> = d.getDiagramElements()
        return diagramElements.keys.map { toUnderlyingInfo(
            it,
            diagramElements[it])
        }
    }

    private fun toUnderlyingInfo(de: DiagramElement, d: Direction?): UnderlyingInfo {
        return UnderlyingInfo(
            de,
            getSideFromDirection(de, d)
        )
    }

    private fun getSideFromDirection(de: DiagramElement, d: Direction?): Side {
        return if (de is BiDirectional<*>) {
            Side.NEITHER
        } else if (de is Rectangular) {
            when (d) {
                Direction.DOWN, Direction.RIGHT -> Side.END
                Direction.UP, Direction.LEFT -> Side.START
                else -> Side.NEITHER
            }
        } else {
            throw LogicException()
        }
    }

    val identifier: String
        get() = "$dimension ($number $underlyingInfo $alignStyle )"


    fun connects(a: Vertex, b: Vertex): Boolean {
        return inSegment(a) && inSegment(b)
    }

    private fun inSegment(b: Vertex): Boolean {
        return verticesOnSlideable.contains(b)
    }



}