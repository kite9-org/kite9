package org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group

import org.kite9.diagram.common.BiDirectional
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.Diagram
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager

/**
 * Represents a single vertex (glyph, context) within the diagram
 */
abstract class AbstractLeafGroup(
    override val connected: Connected?,
    override val container: Container?,
    groupNumber: Int,
    hc: Int,
) :
    AbstractGroup(groupNumber, hc), LeafGroup {

    override fun toString(): String {
        return "[" + groupNumber + connected + "(" + (if (occupiesSpace()) "*" else ".") + (if (container is Diagram) "" else " c: " + container) + "," + axis + ")]"
    }

    override fun contains(lg: Group): Boolean {
        return this === lg
    }

    override fun processLowestLevelLinks(lp: LinkManager.LinkProcessor) {
        processAllLeavingLinks(false, linkManager.allMask(), lp)
    }

    override val height: Int = 0
    override val groupOrdinal: Int = groupNumber
    override val size: Int = 1
    override var layout = container?.getLayout()

    override fun sortLink(
        d: Direction?,
        otherGroup: Group,
        linkValue: Float,
        ordering: Boolean,
        linkRank: Int,
        c: Iterable<BiDirectional<Connected>>
    ) {
        linkManager.sortLink(d, otherGroup, linkValue, ordering, linkRank, c)
    }

    override val hints: Map<String, Float?>
        get() = emptyMap()

    override fun addLeafGroupOrdinalsToSet(s: MutableSet<Int>) {
        s.add(groupNumber)
    }

    override fun occupiesSpace(): Boolean {
        return (connected is Rectangular)
    }
}