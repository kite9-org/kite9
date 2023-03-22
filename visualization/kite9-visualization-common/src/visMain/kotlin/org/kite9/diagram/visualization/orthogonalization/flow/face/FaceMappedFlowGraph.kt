package org.kite9.diagram.visualization.orthogonalization.flow.face

import org.kite9.diagram.common.algorithms.det.UnorderedSet
import org.kite9.diagram.common.algorithms.fg.Node
import org.kite9.diagram.common.elements.edge.Edge
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.visualization.orthogonalization.flow.MappedFlowGraph
import org.kite9.diagram.visualization.planarization.Face
import org.kite9.diagram.visualization.planarization.Planarization

class FaceMappedFlowGraph(pln: Planarization) : MappedFlowGraph(pln) {

    private var facePortionMap: Map<Face, List<PortionNode>>? = null

    fun setFacePortionMap(facePortionMap2: Map<Face, List<PortionNode>>?) {
        facePortionMap = facePortionMap2
    }

    override fun getNodesForEdgePart(f: Face, e: Edge, startVertex: Vertex): Collection<Node> {
        val faceNodes = facePortionMap!![f]!!
        val out: MutableCollection<Node> = UnorderedSet(faceNodes.size * 2)
        val pos = f.indexOf(startVertex, e)
        for (node in faceNodes) {
            if (node.containsFacePart(pos)) {
                out.add(node)
            }
        }
        if (out.size == 0) throw LogicException(
            "Could not find portion for face " + f.getID() + " edge " + e + " start "
                    + startVertex + " at pos " + pos
        )
        if (out.size > 2) {
            throw LogicException(
                "Only two portions should ever meet on the same side of an edge: " + out + " for "
                        + e + " going from " + startVertex
            )
        }
        return out
    }
}