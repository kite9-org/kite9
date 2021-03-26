package org.kite9.diagram.visualization.compaction.rect.second.popout

import org.kite9.diagram.common.algorithms.so.SlackOptimisation
import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.visualization.compaction.UnderlyingInfo
import org.kite9.diagram.visualization.compaction.slideable.ElementSlideable

class BufferSlideable(
    so: SlackOptimisation,
    dimension: Dimension,
    val buffering: List<ElementSlideable>): ElementSlideable(so, dimension, so.getSize()) {

    override val verticesOnSlideable = emptySet<Vertex>()

    override val underlyingInfo: Set<UnderlyingInfo> by lazy {
        buffering
            .flatMap { it.underlyingInfo }
            .toSet()
    }


    override fun toString(): String {
        return "<$dimension B($bufferingIds) $underlyingInfo ${minimum.position},${maximum.position}>"
    }

    val bufferingIds : String by lazy {
        buffering
            .map { it.number.toString() }
            .reduceRight { a, b -> a + ", "+ b}
    }

}