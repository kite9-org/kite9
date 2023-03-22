package org.kite9.diagram.visualization.planarization.mgt.face

import org.kite9.diagram.common.elements.edge.AbstractPlanarizationEdge
import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.common.elements.edge.PlanarizationEdge.RemovalType
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.common.objects.Pair


class TemporaryEdge(from: Vertex, to: Vertex) : AbstractPlanarizationEdge(
    from, to, null
) {
    var above: Rectangular? = null
    var below: Rectangular? = null

    override fun remove() {
        getFrom().removeEdge(this)
        getTo().removeEdge(this)
    }

    val labelEnd: Vertex?
        get() = null

    override fun getCrossCost(): Int {
        return 0 // no cost for crossing temporaries
    }

    override fun removeBeforeOrthogonalization(): RemovalType {
        return RemovalType.YES
    }

    private var layoutEnforcing = false
    override fun isLayoutEnforcing(): Boolean {
        return layoutEnforcing
    }

    override fun setLayoutEnforcing(le: Boolean) {
        layoutEnforcing = le
    }

    override fun split(toIntroduce: Vertex): Pair<PlanarizationEdge> {
        var out = Pair<PlanarizationEdge>(
            TemporaryEdge(getFrom(), toIntroduce),
            TemporaryEdge(toIntroduce, getTo()))
        return out
    }

    override fun isStraightInPlanarization(): Boolean {
        return false
    }

    override fun isPartOf(de: DiagramElement?): Boolean {
        return false
    }

    override fun getDiagramElements(): MutableMap<DiagramElement, Direction?> {
        return mutableMapOf()
    }
}