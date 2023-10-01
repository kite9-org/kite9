package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.common.algorithms.so.SlackOptimisation
import org.kite9.diagram.common.algorithms.so.Slideable
import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.visualization.compaction.Side

enum class Purpose { GUTTER, EDGE, CENTER }

data class Anchor(val e: DiagramElement, val s: Side)

/**
 * This implementation of Slideable tracks underlying diagram
 * elements directly, and also allows for merging with
 * other slideables.  This is done when aligning multiple elements
 * along an axis.
 */
class C2Slideable(
    so: C2SlackOptimisation,
    val dimension: Dimension,
    val purpose: Purpose,
    e: DiagramElement,
    s: Side,
    val number: Int
) : Slideable(so) {

    val anchors: MutableList<Anchor> = mutableListOf(Anchor(e, s))

    fun merge(s: C2Slideable) {
        anchors.addAll(s.anchors)
    }

}