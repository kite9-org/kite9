package org.kite9.diagram.visualization.orthogonalization.flow.container

import org.kite9.diagram.common.algorithms.fg.Arc
import org.kite9.diagram.common.algorithms.fg.LinearArc
import org.kite9.diagram.common.algorithms.fg.Node
import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.visualization.orthogonalization.edge.EdgeConverter
import org.kite9.diagram.visualization.orthogonalization.flow.MappedFlowGraph
import org.kite9.diagram.visualization.orthogonalization.flow.balanced.BalancedFlowOrthogonalizer
import org.kite9.diagram.visualization.orthogonalization.vertex.VertexArranger
import org.kite9.diagram.visualization.planarization.Face
import org.kite9.diagram.visualization.planarization.Planarization
import org.kite9.diagram.visualization.planarization.mgt.BorderEdge

/**
 * Handles the case where a corner of a container has several edges arriving at it.
 *
 * The approach taken is basically the same as a dimensioned vertex.
 *
 * @author robmoffat
 */
class ContainerCornerFlowOrthogonalizer(va: VertexArranger, clc: EdgeConverter) : BalancedFlowOrthogonalizer(
    va, clc
) {

    override fun createFlowGraphForVertex(
        fg: MappedFlowGraph, f: Face, fn: Node, v: Vertex, before: PlanarizationEdge, after: PlanarizationEdge,
        pln: Planarization
    ) {
        if (v is MultiCornerVertex) {
            val vn = checkCreateVertexNode(pln, fg, v, before, after)
            val hn = createHelperNode(fg, f, v, vn, before, after)
            log.send("Creating container vertex $v in portion $fn")
            if (hn != null) {
                createContainerCornerVertexHelperArcs(fg, fn, v, fn, before, after, hn, vn, pln)
            }
        } else {
            super.createFlowGraphForVertex(fg, f, fn, v, before, after, pln)
        }
    }

    private fun createContainerCornerVertexHelperArcs(
        fg: MappedFlowGraph, p: Node, v: MultiCornerVertex, fn: Node, before: PlanarizationEdge,
        after: PlanarizationEdge, hn: Node, vn: Node, pln: Planarization
    ) {
        if (before === after) {
            super.createDimensionedVertexHelperArcs(fg, p, v, fn, before, after, hn, vn, pln)
            return
        }
        val side: BalanceChoice
        side = if (before is BorderEdge || after is BorderEdge) {
            // different side is always true if we are dealing with an edge of the container
            BalanceChoice.DIFFERENT_SIDE_PREFFERED_LAYOUT
        } else {
            decideSide(v, fn, before, after, hn, fg.planarization.edgeOrderings[v]!!.getEdgesAsList())
        }
        log.send(if (log.go()) null else "V: $v Between Edge $before and $after: $side")
        val portionArc = createBalancedPortionArc(fn, hn, side)
        val vertexArc: Arc = LinearArc(TRACE, 4, 0, vn, hn, vn.getID() + "-" + hn.getID())
        addIfNotNull(fg, portionArc)
        addIfNotNull(fg, vertexArc)
    }
}