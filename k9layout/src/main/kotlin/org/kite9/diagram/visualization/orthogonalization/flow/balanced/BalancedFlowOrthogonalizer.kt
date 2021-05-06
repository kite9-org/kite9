package org.kite9.diagram.visualization.orthogonalization.flow.balanced

import org.kite9.diagram.common.algorithms.fg.AbsoluteArc
import org.kite9.diagram.common.algorithms.fg.Arc
import org.kite9.diagram.common.algorithms.fg.LinearArc
import org.kite9.diagram.common.algorithms.fg.Node
import org.kite9.diagram.common.elements.edge.BiDirectionalPlanarizationEdge
import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.common.elements.mapping.ConnectionEdge
import org.kite9.diagram.common.elements.vertex.ConnectedRectangularVertex
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.common.objects.Pair
import org.kite9.diagram.model.Connection
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Terminator
import org.kite9.diagram.visualization.orthogonalization.edge.EdgeConverter
import org.kite9.diagram.visualization.orthogonalization.flow.MappedFlowGraph
import org.kite9.diagram.visualization.orthogonalization.flow.face.ConstrainedFaceFlowOrthogonalizer
import org.kite9.diagram.visualization.orthogonalization.vertex.VertexArranger
import org.kite9.diagram.visualization.planarization.Planarization
import org.kite9.diagram.visualization.planarization.Tools.Companion.isUnderlyingContradicting
import kotlin.math.max

/**
 * Implements several balancing improvements to multi-edge. These are as
 * follows:
 *
 *  1.
 * The system will prefer to balance edges around a vertex, so that there is one
 * edge on each side before adding more to any side (a la Tamassia). Indeed, a
 * bend should be preferred over a vertex having two edges on the same side.
 *  1.
 * The system respects arrow heads, so that a directed arrow body has edges
 * leaving opposite ends
 *  1.
 * If an arrow does not have heads/tails set, then the ends of the arrow should
 * be on opposite sides of the arrow body
 *  1. Corners in containers should cost more than corners outside containers
 *
 *
 * @author robmoffat
 */
open class BalancedFlowOrthogonalizer(va: VertexArranger, clc: EdgeConverter) : ConstrainedFaceFlowOrthogonalizer(
    va, clc
) {

    enum class BalanceChoice(private val ccost: Int) {
        SAME_SIDE_PREFERRED(0), DIFFERENT_SIDE_PREFFERED_LAYOUT(16), DIFFERENT_SIDE_PREFFERED_DIRECTED(8), DIFFERENT_SIDE_PREFFERED_MINOR(
            4
        ),
        OPPOSITE_SIDE_PREFERRED(6);

        fun getCost(): Int {
            return ccost * CORNER
        }
    }

    /**
     * Extends creation for special arrow rules. The head of the arrow and the
     * tail(s) must be on opposite sides of the arrow. To ensure this, we look
     * for cases where there is one head or one tail arrow. This is manipulated
     * so that only helper nodes to the sides of the one arrow are allowed to
     * receive corners.
     */
    override fun createDimensionedVertexHelperArcs(
        fg: MappedFlowGraph?, p: Node?, v: Vertex?, fn: Node?, before: PlanarizationEdge?,
        after: PlanarizationEdge?, hn: Node, vn: Node, pln: Planarization?
    ) {
        if (before === after) {
            super.createDimensionedVertexHelperArcs(fg, p, v, fn, before, after, hn, vn, pln)
            return
        }

        // side being true indicates that we want the edges to be on opposite
        // sides of the vertex.
        // if side is false, then we just want them on different sides, not
        // necessarily opposite
        var side = BalanceChoice.DIFFERENT_SIDE_PREFFERED_MINOR
        side = decideSide(v, fn, before, after, hn, fg!!.planarization.edgeOrderings[v]!!.getEdgesAsList())
        log.send(if (log.go()) null else "V: $v Between Edge $before and $after: $side")
        val portionArc = createBalancedPortionArc(fn, hn, side)
        val vertexArc: Arc = LinearArc(TRACE, 4, 0, vn, hn, vn.getID() + "-" + hn.getID())
        addIfNotNull(fg, portionArc)
        addIfNotNull(fg, vertexArc)
    }

    protected fun createBalancedPortionArc(fn: Node?, hn: Node, side: BalanceChoice): Arc {
        return when (side) {
            BalanceChoice.OPPOSITE_SIDE_PREFERRED -> {
                // this arc has a high cost if you try and put anything into it
                AbsoluteArc(side.getCost(), 2, fn!!, hn, fn.getID() + "-" + hn.getID())
            }
            BalanceChoice.DIFFERENT_SIDE_PREFFERED_LAYOUT, BalanceChoice.DIFFERENT_SIDE_PREFFERED_MINOR, BalanceChoice.DIFFERENT_SIDE_PREFFERED_DIRECTED -> {
                // this arc is very happy with you pushing -1, 0 or 1 edge from the
                // face. You can push 2 or -2, but this would
                // mean that you end up with edges on the same side of the vertex.
                StepCostArc(TRACE, 2, fn!!, hn, fn.getID() + "-" + hn.getID(), 1, side.getCost())
            }
            BalanceChoice.SAME_SIDE_PREFERRED -> {

                // this arc is minimal cost so you can push 2 into it and have edges
                // on the same side as each other
                AbsoluteArc(TRACE, 2, fn!!, hn, fn.getID() + "-" + hn.getID())
            }
            else -> {
                AbsoluteArc(TRACE, 2, fn!!, hn, fn.getID() + "-" + hn.getID())
            }
        }
    }

    private fun opposite(a: PlanarizationEdge?, b: PlanarizationEdge?): Boolean {
        val aStyle: Pair<Terminator?> = getEdgeStyle(a)
        val bStyle: Pair<Terminator?> = getEdgeStyle(b)
        return !aStyle.equals(bStyle)
    }

    protected fun decideSide(
        v: Vertex?,
        fn: Node?,
        before: PlanarizationEdge?,
        after: PlanarizationEdge?,
        hn: Node?,
        listOfEdges: List<PlanarizationEdge?>
    ): BalanceChoice {
        // where only 2 edges, make the arrow ends come out opposite, preferably		
        if (before!!.isLayoutEnforcing() || after!!.isLayoutEnforcing()) {
            return BalanceChoice.DIFFERENT_SIDE_PREFFERED_LAYOUT
        }
        if (isUnderlyingContradicting(before) || isUnderlyingContradicting(after)) {
            // short-cuts the process if there is a contradiction
            return BalanceChoice.SAME_SIDE_PREFERRED
        }
        if (isStraight(before) || isStraight(after)) {
            return BalanceChoice.DIFFERENT_SIDE_PREFFERED_DIRECTED
        }

        // this is used for arrows - try and make heads and tails appear opposite sides
        if (v is ConnectedRectangularVertex && v.isSeparatingConnections()) {
            if (listOfEdges.size <= 2) {
                // place edges on opposite sides
                return BalanceChoice.OPPOSITE_SIDE_PREFERRED
            }
            if (opposite(before, after)) {
                return BalanceChoice.OPPOSITE_SIDE_PREFERRED
            }
        }

        // for four edges or less, try and get them all on different sides, as that 
        // deforms the arrows less
        return if (listOfEdges.size <= 4) {
            // place each edge on a different side, ideally
            BalanceChoice.DIFFERENT_SIDE_PREFFERED_MINOR
        } else BalanceChoice.SAME_SIDE_PREFERRED
    }

    private fun isStraight(before: PlanarizationEdge?): Boolean {
        return before!!.getDrawDirection() != null && !isUnderlyingContradicting(before)
    }

    private fun getEdgeStyle(en: PlanarizationEdge?): TerminatorPair {
        if (en is BiDirectionalPlanarizationEdge) {
            val und = en.getOriginalUnderlying()
            if (und is Connection) {
                return TerminatorPair(und.getFromDecoration(), und.getToDecoration())
            }
        }
        return TerminatorPair(null, null)
    }

    override fun weightCost(e: PlanarizationEdge): Int {
        return (e as? ConnectionEdge)?.let {
            // this tries to keep corners inside containers
            getDepthBasedWeight(it)
        } ?: super.weightCost(e)
    }

    private fun getDepthBasedWeight(e: PlanarizationEdge): Int {
        val depthFrom = getVertexMaxDepth(e.getFrom())
        val depthTo = getVertexMaxDepth(e.getTo())
        val depth = max(depthFrom, depthTo)
        val orig = super.weightCost(e)
        return orig - depth * 10
    }

    private fun getVertexMaxDepth(v: Vertex): Int {
        return v.getDiagramElements()
            .map { de: DiagramElement -> de.getDepth() }
            .reduceOrNull { a: Int, b: Int ->
                max(
                    a!!, b!!
                )
            }?.toInt() ?: 0
    }

    companion object {
        const val UNBALANCED_VERTEX_COST = 4 * CORNER
    }
}