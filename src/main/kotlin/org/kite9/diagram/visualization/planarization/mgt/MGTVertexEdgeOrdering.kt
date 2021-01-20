package org.kite9.diagram.visualization.planarization.mgt

import org.kite9.diagram.common.elements.edge.Edge
import org.kite9.diagram.visualization.planarization.Tools.Companion.isUnderlyingContradicting
import org.kite9.diagram.visualization.planarization.ordering.AbstractCachingEdgeOrdering
import org.kite9.diagram.visualization.planarization.ordering.VertexEdgeOrdering
import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.model.position.Direction
import java.util.Collections
import org.kite9.diagram.visualization.planarization.ordering.EdgeOrdering
import java.util.ArrayList

class MGTVertexEdgeOrdering(val pl: MGTPlanarization, val v: Vertex) : AbstractCachingEdgeOrdering(),
    VertexEdgeOrdering {

    var af: MutableList<PlanarizationEdge> = pl.getAboveForwardLinks(v)
    var ab: MutableList<PlanarizationEdge> = pl.getAboveBackwardLinks(v)
    var bf: MutableList<PlanarizationEdge> = pl.getBelowForwardLinks(v)
    var bb: MutableList<PlanarizationEdge> = pl.getBelowBackwardLinks(v)

    override fun remove(toRemove: PlanarizationEdge) {
        safeRemove(toRemove, af)
        safeRemove(toRemove, bf)
        safeRemove(toRemove, bb)
        safeRemove(toRemove, ab)
        changed()
    }

    private fun safeRemove(toRemove: PlanarizationEdge, l: MutableList<PlanarizationEdge>) {
        l.remove(toRemove)
    }

    override fun size(): Int {
        return af.size + bf.size + bb.size + ab.size
    }

    override val edgesAsListInner: List<PlanarizationEdge>
        protected get() {
            val out: MutableList<PlanarizationEdge> = ArrayList(size())
            addAllBackwards(out, af)
            out.addAll(bf)
            addAllBackwards(out, bb)
            out.addAll(ab)
            directions = null
            for (edge in out) {
                addEdgeDirection(edge.getDrawDirectionFrom(v), isUnderlyingContradicting(edge))
            }
            return Collections.unmodifiableList(out)
        }

    private fun addAllBackwards(out: MutableList<PlanarizationEdge>, af2: List<PlanarizationEdge>) {
        for (i in af2.indices.reversed()) {
            out.add(af2[i])
        }
    }

    override fun toString(): String {
        return "[VEO:" + (if (getEdgeDirections() === EdgeOrdering.MUTLIPLE_DIRECTIONS) "MULTI" else getEdgeDirections()) + ":" + getEdgesAsList() + "]"
    }

    override fun replace(b: PlanarizationEdge, a: PlanarizationEdge) {
        replace(af, b, a)
        replace(bf, b, a)
        replace(bb, b, a)
        replace(ab, b, a)
        changed()
    }

    private fun replace(ml: MutableList<PlanarizationEdge>, b: PlanarizationEdge, a: PlanarizationEdge): Boolean {
        val bIndex = ml.indexOf(b)
        return if (bIndex != -1) {
            ml.set(bIndex, a)
            true
        } else {
            false
        }
    }

    override fun getInterceptDirection(e: Edge): Direction {
        return e.getDrawDirectionFrom(v)!!
    }

}