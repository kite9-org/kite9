package org.kite9.diagram.visualization.orthogonalization.flow

import org.kite9.diagram.common.algorithms.fg.*
import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.visualization.orthogonalization.edge.EdgeConverter
import org.kite9.diagram.visualization.orthogonalization.vertex.VertexArranger
import org.kite9.diagram.visualization.planarization.Face
import org.kite9.diagram.visualization.planarization.Planarization
import org.kite9.diagram.visualization.planarization.Tools.Companion.isUnderlyingContradicting
import java.util.*

/**
 * This class builds a corner-flow network to model the current Planarization.
 * It is capable of handling any degree vertex, and also edge constraints where
 * an edge must be oriented in a certain direction within the diagram.
 *
 * The basic approach is that faces, edges, vertices and face-vertice boundaries (helpers) are all nodes.
 * Faces and vertices have 4 corners to dispose of.  Helpers can sink 2.  Corners can be pushed over edge nodes.
 * Arcs link up the various parts.
 *
 *
 * @author robmoffat
 */
abstract class AbstractFlowOrthogonalizer(va: VertexArranger, clc: EdgeConverter) : MappedFlowOrthogonalizer(
    va, clc
) {

    var nodeNo = 0

    protected fun initFlowGraph(pln: Planarization, fg: MappedFlowGraph) {
        log.send(if (log.go()) null else "Planarization to convert:$pln")
        log.send(if (log.go()) null else pln!!.faces.toString())

        // create face, vertex, edge nodes and all of the arcs between them and
        // the portion nodes
        for (f in pln!!.faces) {
            if (faceRequiresFlowGraph(f)) {
                createFlowGraphForFace(pln, fg, f)
            }
        }

        // output detail in log
        val nodes: MutableCollection<String?> = TreeSet()
        for (n in fg.allNodes) {
            nodes.add(n.getID() + " has " + n.arcs.size + " arcs: " + n.arcs)
        }
        log.send(if (log.go()) null else "Node details: ", nodes)
    }

    interface VertexHandler {
        fun processVertex(i: PlanarizationEdge, o: PlanarizationEdge, v: Vertex, face: Node)
    }

    protected abstract fun createFlowGraphForVertex(
        fg: MappedFlowGraph,
        f: Face,
        p: Node,
        v: Vertex,
        before: PlanarizationEdge,
        after: PlanarizationEdge,
        pln: Planarization
    )

    protected fun createFlowGraphForFace(pln: Planarization, fg: MappedFlowGraph, f: Face) {
        createFaceNodes(fg, f, pln, object : VertexHandler {
            override fun processVertex(i: PlanarizationEdge, out: PlanarizationEdge, v: Vertex, current: Node) {
                createFlowGraphForVertex(fg, f, current, v, i, out, pln)
            }
        })
    }

    protected abstract fun createFaceNodes(
        fg: MappedFlowGraph,
        f: Face,
        pln: Planarization,
        vertexHandler: VertexHandler
    )

    protected fun addIfNotNull(fg: MappedFlowGraph, a: Arc?) {
        if (a != null) fg.allArcs.add(a)
    }

    protected fun createHelperNode(
        fg: MappedFlowGraph,
        f: Face,
        v: Vertex,
        vn: Node?,
        before: PlanarizationEdge,
        after: PlanarizationEdge
    ): Node {
        // this is the number of corners the vertex can take from the face
        // between these two edges
        val supply = -2
        val ff = createFaceVertex(f, v, before, after)
        val hn: Node = SimpleNode("h" + nodeNo++ + "[" + v.getID() + "/" + f.getID() + "]", supply, ff)
        fg.setNodeFor(ff, hn)
        hn.type = HELPER_NODE
        return hn
    }

    protected open fun weightCost(e: PlanarizationEdge): Int {
        return CORNER * e.getBendCost() + TRACE
    }

    override val prefix: String
        get() = "PLAN"
    override val isLoggingEnabled: Boolean
        get() = false

    fun faceRequiresFlowGraph(f: Face): Boolean {
        return f.edgeCount() > 0
    }

    /**
     * first stage - maximise the flows to get an approximate solution
     */
    protected fun maximiseFlow(fg: MappedFlowGraph) {
        val ssp: FlowGraphSPP<MappedFlowGraph> = RapidFlowGraphSSP()
        ssp.displayFlowInformation(fg)
        ssp.maximiseFlow(fg)
        checkFlows(fg)
    }

//    protected abstract fun createFlowGraphObject(pln: Planarization?): MappedFlowGraph

    companion object {
        const val VERTEX_NODE = "vn"
        const val PORTION_NODE = "pn"
        const val HELPER_NODE = "hn"
        const val EDGE_NODE = "en"

        /**
         * this is added to ensure that the algorithm doesn't track about all over
         * the shop picking longer and longer routes
         */
        const val TRACE = 1

        /**
         * This is the cost of inserting a corner on an edge
         */
        const val CORNER = 100
        @JvmStatic
		fun createFaceVertex(from: Face?, to: Vertex, before: PlanarizationEdge, after: PlanarizationEdge): FaceVertex {
            val ff = FaceVertex(from!!, to, before, after)
            if (!before.meets(to) || !after.meets(to)) {
                throw LogicException("Edges must meet the vertex in the face $ff")
            }
            return ff
        }

        @JvmStatic
		fun createEdgeVertex(
            e: PlanarizationEdge?,
            to: Vertex?
        ): EdgeVertex {
            return if (to == null) EdgeVertex(e!!) else EdgeVertex(e!!, to)
        }

        @JvmStatic
		fun removeArcs(fg: MappedFlowGraph, n: Node) {
            for (a in n.arcs) {
                val otherEnd = if (a.from === n) a.to else a.from
                otherEnd.arcs.remove(a)
                fg.allArcs.remove(a)
            }
            n.arcs.clear()
        }

        @JvmStatic
		fun isConstrained(e: PlanarizationEdge): Boolean {
            return e.getDrawDirection() != null && !isUnderlyingContradicting(e)
        }
    }
}