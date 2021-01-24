package org.kite9.diagram.visualization.orthogonalization.flow.face

import org.kite9.diagram.common.algorithms.fg.AbsoluteArc
import org.kite9.diagram.common.algorithms.fg.Arc
import org.kite9.diagram.common.algorithms.fg.Node
import org.kite9.diagram.common.algorithms.fg.SimpleNode
import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.visualization.orthogonalization.edge.EdgeConverter
import org.kite9.diagram.visualization.orthogonalization.flow.MappedFlowGraph
import org.kite9.diagram.visualization.orthogonalization.flow.vertex.ConstrainedVertexFlowOrthogonalizer
import org.kite9.diagram.visualization.orthogonalization.vertex.VertexArranger
import org.kite9.diagram.visualization.planarization.Face
import org.kite9.diagram.visualization.planarization.Planarization

/**
 * Handles the creation of edges and faces in the flow graph.
 *
 * @author robmoffat
 */
open class ConstrainedFaceFlowOrthogonalizer(va: VertexArranger, clc: EdgeConverter) :
    ConstrainedVertexFlowOrthogonalizer(
        va, clc
    ) {

    var constraints: ConstraintGroup? = null
    var facePortionMap: MutableMap<Face, List<PortionNode>> = HashMap()
    var faceNodes: MutableCollection<SubdivisionNode> = mutableListOf()

    override fun createOptimisedFlowGraph(pln: Planarization): MappedFlowGraph {
        val sffr = ConstraintGroupGenerator()
        val constraints = sffr.getAllFloatingAndFixedConstraints(pln)
        this.constraints = constraints

        // create portions and their nodes.
        for (f in pln.faces) {
            if (faceRequiresFlowGraph(f)) {
                val portions = createFacePortionNodes(f, constraints, pln)
                facePortionMap[f] = portions
            }
        }

        // repository for any generated constraints
        val fg = FaceMappedFlowGraph(pln);
        fg.setFacePortionMap(facePortionMap)

        // create constraints which will subdivide the faces
        initFlowGraph(pln, fg)
        maximiseFlow(fg)

        // nudge the graph so that face constraints are met.
        val cn: ConstraintNudger = SequentialConstrainedFlowNudger(facePortionMap)
        cn.processNudges(fg, constraints, faceNodes)
        return fg
    }

    protected fun createFaceSubdivisionNode(fg: MappedFlowGraph, f: Face): Node {
        var fn = fg.getNodeFor(f) as SubdivisionNode?
        if (fn != null) return fn
        val supply = if (f.isOuterFace) -4 else 4
        fn = SubdivisionNode("f[" + f.getID() + (if (f.isOuterFace) "x" else "") + "]", supply)
        fg.setNodeFor(f, fn)
        faceNodes.add(fn)
        return fn
    }

    protected fun checkCreateEdgeNode(fg: MappedFlowGraph?, e: PlanarizationEdge?, near: Vertex?, id: String): Node {
        val ev = createEdgeVertex(e, near)
        var en = fg!!.getNodeFor(ev)
        if (en != null) {
            return en
        }
        en = SimpleNode("e[$id]", 0, ev)
        en.type = EDGE_NODE
        fg.setNodeFor(ev, en)
        return en
    }

    protected fun createFlowGraphForEdge(fg: MappedFlowGraph, e: PlanarizationEdge, fn: Node, i: Int) {
        val current = fn as PortionNode
        if (isConstrained(constraints!!, e)) {
            // when the edge is constrained by more than 2
            // portions, we need
            // to stop corners bleeding into portions they
            // are diagonally separated from
            // so we create multiple edge nodes
            if (current.containsVertexForEdge(e, e.getFrom())) {
                createPortionEdgeLink(e, current, e.getFrom(), fg, "-A", i)
            } else if (current.containsVertexForEdge(e, e.getTo())) {
                createPortionEdgeLink(e, current, e.getTo(), fg, "-B", i)
            } else {
                throw LogicException(
                    "Portion should contain one end of the edge: " + current
                            + " " + e
                )
            }
        } else {
            createPortionEdgeLink(e, current, null, fg, "", i)
        }
    }

    protected fun isConstrained(constraints: ConstraintGroup, e: PlanarizationEdge): Boolean {
        return constraints.isConstrained(e)
    }

    override fun createFaceNodes(fg: MappedFlowGraph, f: Face, pln: Planarization, vertexHandler: VertexHandler) {
        val fn = createFaceSubdivisionNode(fg, f)
        for (current in facePortionMap[f]!!) {
            val faceArc = createFaceToPortionArc(fg, fn, current)
            current.faceArc = faceArc
            var start = current.edgeStartPosition
            var count = current.edgeEndPosition - current.edgeStartPosition
            if (count <= 0) {
                count += current.face.edgeCount()
            }
            if (start == -1) {
                start = 0
            }
            var c = 0
            while (c < count) {
                val i = (c + start) % current.face.edgeCount()
                val `in` = current.face.getBoundary(i)
                val out = current.face.getBoundary(i + 1)
                val v = current.face.getCorner(i + 1)
                vertexHandler.processVertex(`in`, out, v, current)
                c++
            }
            for (i in 0..count) {
                val e = current.getEdge(i) as PlanarizationEdge
                if (!isConstrained(e)) {
                    createFlowGraphForEdge(fg, e, current, i)
                }
            }
           // fg.setNodeFor(null, current)
        }
    }

    /**
     * Create arc from face node to portion node.  (A face is made up of several portions)
     */
    protected fun createFaceToPortionArc(fg: MappedFlowGraph?, fn: Node, pn: Node?): Arc {
        val aa = AbsoluteArc(TRACE, Int.MAX_VALUE, fn, pn!!, fn.getID() + "-" + pn.getID())
        fg!!.allArcs.add(aa)
        return aa
    }

    protected fun createPortionEdgeLink(
        e: PlanarizationEdge,
        portion: PortionNode,
        end: Vertex?,
        fg: MappedFlowGraph,
        suffix: String,
        i: Int
    ) {
        val en = checkCreateEdgeNode(fg, e, end, e.toString() + suffix)
        val arcs = createPortionEdgeArcs(portion, e, en)
        fg.allArcs.addAll(arcs)
    }

    protected fun createPortionEdgeArcs(fn: Node, e: PlanarizationEdge, en: Node): List<Arc> {
        val l: MutableList<Arc> = mutableListOf()
        val weightCost = weightCost(e)
        val aa: Arc
        aa = AbsoluteArc(weightCost, Int.MAX_VALUE, fn, en, fn.getID() + "-" + e.toString())
        log.send(if (log.go()) null else "Edge Arc: " + e + " cost: " + weightCost + " (part of " + e.getDiagramElements().keys + ")")
        l.add(aa)
        return l
    }

    private fun createFacePortionNodes(
        f: Face,
        constraints: ConstraintGroup,
        pln: Planarization
    ): List<PortionNode> {
        log.send(if (log.go()) null else "Creating portions for " + (if (f.isOuterFace) "outer" else "inner") + " face " + f.getID() + ": " + f.cornerIterator())
        val constrainedEdgesForFace = getConstraintsForFace(constraints, f)

        // first, divide up the faces into portions, bounded by constrained edges
        val portions = createPortions(constrainedEdgesForFace, f)
        log.send("Portions for " + f.getID(), portions)
        return portions
    }

    private fun getConstraintsForFace(constraints: ConstraintGroup, f: Face): List<Int>? {
        return constraints.getConstraintsRequiredForFace(f)
    }

    /**
     * Subdivides the edges from a face or vertex into portions, bounded by constraining edges.
     */
    private fun createPortions(constraintEdges: List<Int>?, f: Face): List<PortionNode> {
        if (constraintEdges == null || constraintEdges.size < 2) {
            return listOf(PortionNode("p[" + f.getID() + "p0" + (if (f.isOuterFace) "x" else "") + "]", 0, f, -1, -1))
        }
        val portions: MutableList<PortionNode> = ArrayList(constraintEdges.size)
        for (i in constraintEdges.indices) {
            val prev = if (i == 0) constraintEdges[constraintEdges.size - 1] else constraintEdges[i - 1]
            val current = constraintEdges[i]
            val toAdd = PortionNode(
                "p[" + f.getID() + "p" + portions.size + (if (f.isOuterFace) "x" else "") + "]",
                0,
                f,
                prev,
                current
            )
            log.send("Created portion: " + toAdd + " starts at=" + toAdd.constrainedEdgeStart + " ends at=" + toAdd.constrainedEdgeEnd + " se=" + toAdd.edgeStartPosition + " ee=" + toAdd.edgeEndPosition)
            portions.add(toAdd)
        }
        log.send(if (log.go()) null else "Portion $portions")
        return portions
    }

    companion object {
        const val FACE_SUBDIVISION_NODE = "fn"
    }
}