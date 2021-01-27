package org.kite9.diagram.visualization.orthogonalization.flow

import org.kite9.diagram.common.algorithms.det.UnorderedSet
import org.kite9.diagram.common.algorithms.fg.Arc
import org.kite9.diagram.common.algorithms.fg.FlowGraph
import org.kite9.diagram.common.algorithms.fg.Node
import org.kite9.diagram.common.elements.edge.Edge
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.visualization.planarization.Face
import org.kite9.diagram.visualization.planarization.Planarization

/**
 * Contains the mapping of the flow graph to the multi-edge methodology.
 *
 *  * Face nodes are connected to portion nodes
 *  * Portion nodes are connected to helper nodes and edge nodes
 *  * Helper nodes are connected to vertex nodes
 *
 *
 * Faces push 4 or -4 (outer faces) corners through the portions, and into the
 * helper nodes of vertices. They can push to other portions via edges.
 *
 * @author robmoffat
 */
abstract class MappedFlowGraph(val planarization: Planarization) : FlowGraph(ArrayList()) {

    override fun toString(): String {
        return """
            ${super.toString()}
            ${outputValueArcs()}
            """.trimIndent()
    }

    private fun outputValueArcs(): String {
        val sb = StringBuilder()
        for (a in valueArcs) {
            sb.append(a)
            sb.append(":(")
            sb.append(a.flow)
            sb.append(")\n")
        }
        return sb.toString()
    }

    // ignore
    val valueArcs: Set<Arc>
        get() {
            val out: MutableSet<Arc> = UnorderedSet()
            for (arc in allArcs) {
                if (arc.flow == 0) {
                    // ignore
                } else {
                    out.add(arc)
                }
            }
            return out
        }

    private val map: MutableMap<Any, Node> = HashMap()

    fun getNodeFor(o: Any): Node? {
        return map[o]
    }

    fun setNodeFor(o: Any, n: Node) {
        map[o] = n
        allNodes.add(n)
        allArcs.addAll(n.arcs)
    }

    abstract fun getNodesForEdgePart(f: Face, e: Edge, startVertex: Vertex): Collection<Node>
}