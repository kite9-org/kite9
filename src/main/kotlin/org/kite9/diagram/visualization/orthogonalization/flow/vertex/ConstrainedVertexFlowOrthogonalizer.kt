package org.kite9.diagram.visualization.orthogonalization.flow.vertex

import org.kite9.diagram.common.BiDirectional
import org.kite9.diagram.common.algorithms.fg.LinearArc
import org.kite9.diagram.common.algorithms.fg.Node
import org.kite9.diagram.common.algorithms.fg.SimpleNode
import org.kite9.diagram.common.elements.edge.BiDirectionalPlanarizationEdge
import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.common.elements.vertex.ConnectedVertex
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Direction.Companion.reverse
import org.kite9.diagram.model.position.Direction.Companion.rotateAntiClockwise
import org.kite9.diagram.visualization.orthogonalization.edge.EdgeConverter
import org.kite9.diagram.visualization.orthogonalization.flow.AbstractFlowOrthogonalizer
import org.kite9.diagram.visualization.orthogonalization.flow.MappedFlowGraph
import org.kite9.diagram.visualization.orthogonalization.vertex.VertexArranger
import org.kite9.diagram.visualization.planarization.Face
import org.kite9.diagram.visualization.planarization.Planarization
import org.kite9.diagram.visualization.planarization.ordering.EdgeOrdering
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * This handles the creation of vertex nodes in the flow graph.  Where a vertex has > 1 directed edges entering or leaving, and
 * we can work out how many turns there are between these edges, we create extra vertex nodes with the correct number of
 * available turns in its source.
 *
 *
 * @author robmoffat
 */
abstract class ConstrainedVertexFlowOrthogonalizer(va: VertexArranger, clc: EdgeConverter) :
    AbstractFlowOrthogonalizer(
        va, clc
    ) {

    var nextId = 0

    data class VertexDivision(
        val from: PlanarizationEdge,
        val to: PlanarizationEdge,
        val containing: List<PlanarizationEdge>,
        val corners: Int,
        val id: Int) {

        fun useFor(from: PlanarizationEdge?, to: PlanarizationEdge?): Boolean {
            return (this.from === from || containing.contains(from)) &&
                    (this.to === to || containing.contains(to))
        }

        override fun toString(): String {
            return "VertexDivision [corners=$corners, from=$from, id=$id, to=$to, containing=$containing]"
        }
    }

    private val vertexDivisions: MutableMap<Vertex, List<VertexDivision>> = HashMap()

    override fun createFlowGraphForVertex(
        fg: MappedFlowGraph,
        f: Face,
        fn: Node,
        v: Vertex,
        before: PlanarizationEdge,
        after: PlanarizationEdge,
        pln: Planarization
    ) {
        val vn = checkCreateVertexNode(pln, fg, v, before, after)
        val hn = createHelperNode(fg, f, v, vn, before, after)
        log.send("Creating vertex $v in portion $fn")
        if (hn != null) {
            if (v.hasDimension()) {
                createDimensionedVertexHelperArcs(fg, fn, v, fn, before, after, hn, vn, pln)
            } else {
                createDimensionlessVertexHelperArcs(fg, fn, v, fn, before, after, hn, vn, pln)
            }
        }
    }

    protected fun checkCreateVertexNode(
        pln: Planarization,
        fg: MappedFlowGraph,
        v: Vertex,
        before: PlanarizationEdge,
        after: PlanarizationEdge
    ): Node {
        val divs = checkCreateVertexDivisions(pln, fg, v)
        var memento: Any? = null
        val id: String
        var supply = 4
        if (divs!!.size > 1) {
            val vd = getDivStartEdge(pln, divs, before, after, v)
            memento = VertexPart(v!!, vd!!)
            id = "v[" + v.getID() + ":" + vd.id + "]"
            supply = vd.corners
        } else {
            memento = v
            id = "v[" + v!!.getID() + "]"
        }
        val an = fg!!.getNodeFor(memento)
        if (an != null) return an
        val vn: Node = SimpleNode(id, supply, memento)
        vn.type = VERTEX_NODE
        fg.setNodeFor(memento, vn)
        log.send("Creating vertex node $vn with supply $supply between $before and $after")
        return vn
    }

    private fun countAntiClockwiseTurns(
        v: Vertex,
        before: PlanarizationEdge?,
        after: PlanarizationEdge?,
        pln: Planarization?
    ): Int? {
        var d1 = before!!.getDrawDirectionFrom(v)
        val d2 = after!!.getDrawDirectionFrom(v)
        val eo = pln!!.edgeOrderings[v]
        if (d1 === d2 && eo!!.getEdgeDirections() is Direction) {
            return countAntiClockwiseTurnsByPlanarizationPosition(d1, pln, before, after, v)
        }
        var turns = 0
        while (d1 !== d2) {
            d1 = rotateAntiClockwise(d1!!)
            turns++
        }
        log.send("Anticlockwise Turns between $before and $after at $v = $turns")
        return turns
    }

    /**
     * Returns either zero or 4 turns, or null if the order can't be ascertained.
     */
    private fun countAntiClockwiseTurnsByPlanarizationPosition(
        d1: Direction?, pln: Planarization?, before: PlanarizationEdge?,
        after: PlanarizationEdge?, v: Vertex
    ): Int? {
        if (notStraight(before) || notStraight(after)) {
            log.send("Not Setting Turns between $before and $after as edges aren't straight")
            return null
        }
        val vUnd = (v as ConnectedVertex).getOriginalUnderlying()
        val beforeUnd =
            (before as BiDirectionalPlanarizationEdge?)!!.getOriginalUnderlying() as BiDirectional<Connected>
        val afterUnd = (after as BiDirectionalPlanarizationEdge?)!!.getOriginalUnderlying() as BiDirectional<Connected>
        val beforeConnected = beforeUnd.otherEnd(vUnd)
        val afterConnected = afterUnd.otherEnd(vUnd)
        val beforeRI = pln!!.getPlacedPosition(beforeConnected)
        val afterRI = pln.getPlacedPosition(afterConnected)
        val xc = beforeRI!!.compareX(afterRI!!)
        val yc = beforeRI.compareY(afterRI)
        when (d1) {
            Direction.UP -> when (xc) {
                -1 -> return 4
                1 -> return 0
                0 -> return null
            }
            Direction.DOWN -> when (xc) {
                -1 -> return 0
                1 -> return 4
                0 -> return null
            }
            Direction.LEFT -> when (yc) {
                -1 -> return 0
                1 -> return 4
                0 -> return null
            }
            Direction.RIGHT -> when (yc) {
                -1 -> return 4
                1 -> return 0
                0 -> return null
            }
        }
        throw LogicException("Problem identifying turns = no direction")
    }

    private fun notStraight(before: PlanarizationEdge?): Boolean {
        return if (before is PlanarizationEdge) {
            !before.isStraightInPlanarization()
        } else true
    }

    private fun getDivStartEdge(
        pln: Planarization,
        divs: List<VertexDivision>,
        before: PlanarizationEdge,
        after: PlanarizationEdge,
        v: Vertex
    ): VertexDivision {
        for (vertexDivision in divs) {
            if (vertexDivision.useFor(after, before)) {
                return vertexDivision
            }
        }
        throw LogicException("Couldn't find correct division for $before and $after around $v")
    }

    private fun checkCreateVertexDivisions(
        pln: Planarization,
        fg: MappedFlowGraph,
        v: Vertex
    ): List<VertexDivision>? {
        var divs = vertexDivisions[v]
        if (divs == null) {

            // first, get only directed edges
            val edgeOrdering = pln.edgeOrderings[v]!!
            if (edgeOrdering.getEdgeDirections() != null) {
                divs = createDirectedMap(v, edgeOrdering, pln)
                if (divs.size > 0) {
                    assertFourTurns(divs)
                }
            } else {
                // we can't subdivide the vertex
                divs = emptyList()
            }
            vertexDivisions[v] = divs
            log.send("----------------------")
            log.send("Vertex Divisions for $v: ", divs)
        }
        return divs
    }

    private fun assertFourTurns(divs: List<VertexDivision?>?) {
        var total = 0
        for (vertexDivision in divs!!) {
            total += vertexDivision!!.corners
        }
        if (total != 4) {
            log.send("Vertex Division Problem: ", divs)
            throw LogicException("Should be four turns around vertex")
        }
    }

    /**
     * Means we get one vertex between each directed edge leaving it
     */
    private fun createDirectedMap(v: Vertex, edgeOrdering: EdgeOrdering?, pln: Planarization): List<VertexDivision> {
        val out: MutableList<VertexDivision> = mutableListOf()
        val basic = createDirectedList(edgeOrdering)
        if (basic.size < 2) {
            return emptyList()
        }
        val edgesAsList = edgeOrdering!!.getEdgesAsList()
        val offset = edgesAsList.indexOf(basic[0])

        var currentEdge : PlanarizationEdge? = null
        var nextEdge : PlanarizationEdge? = null
        var containing : MutableList<PlanarizationEdge> = mutableListOf()

        for (current in 0 until edgeOrdering.size()) {
            val now = (current + offset + edgeOrdering.size()) % edgeOrdering.size()
            val next = (current + offset + 1 + edgeOrdering.size()) % edgeOrdering.size()

            if (isConstrained(edgesAsList[now])) {
                currentEdge = edgesAsList[now]
                containing = mutableListOf()
            }
            if (isConstrained(edgesAsList[next])) {
                nextEdge = edgesAsList[next]
                val turns = countAntiClockwiseTurns(v, nextEdge, currentEdge, pln) ?: return emptyList()
                val open = VertexDivision(currentEdge!!, nextEdge, containing, turns, nextId++)
                out.add(open)
            } else {
                containing.add(edgesAsList[next])
            }
        }
        return out
    }

    private fun createDirectedList(edgeOrdering: EdgeOrdering?): List<PlanarizationEdge> {
        val out: MutableList<PlanarizationEdge> = ArrayList()
        val iterator = edgeOrdering!!.getEdgesAsList().iterator()
        while (iterator.hasNext()) {
            val edge = iterator.next()
            if (isConstrained(edge)) {
                out.add(edge)
            }
        }
        return out
    }

    /**
     * Where the vertex has no dimension, only 1 or 0
     * corners can be pushed in or out, meaning that all edges entering the
     * vertex must be on separate sides.
     */
    protected fun createDimensionlessVertexHelperArcs(
        fg: MappedFlowGraph?, p: Node?, v: Vertex?, fn: Node?, before: PlanarizationEdge?,
        after: PlanarizationEdge?, hn: Node, vn: Node, pln: Planarization?
    ) {
        val canCorner = canCorner(v!!, before, after)
        log.send(if (log.go()) null else "Dimensionless Vertex: $v before: $before after: $after corners: 1")
        if (canCorner) {
            val a4 = LinearArc(TRACE, 1, -1, fn!!, hn, fn.getID() + "-" + hn.getID())
            addIfNotNull(fg!!, a4)
        }
        val a2 = LinearArc(TRACE, 4, 0, vn, hn, vn.getID() + "-" + hn.getID())
        addIfNotNull(fg!!, a2)
    }

    /**
     * Creates the two arcs from the helper node to the vertex and portion respectively.
     * @param vn
     * @param a2
     */
    protected open fun createDimensionedVertexHelperArcs(
        fg: MappedFlowGraph?, p: Node?, v: Vertex?, fn: Node?, before: PlanarizationEdge?,
        after: PlanarizationEdge?, hn: Node, vn: Node, pln: Planarization?
    ) {
        val a4 = LinearArc(TRACE, 2, -2, fn!!, hn, fn.getID() + "-" + hn.getID())
        val a2 = LinearArc(TRACE, 4, 0, vn, hn, vn.getID() + "-" + hn.getID())
        addIfNotNull(fg!!, a4)
        addIfNotNull(fg, a2)
    }

    /**
     * Decide side for container edges and edge crossing vertexes
     */
    private fun canCorner(v: Vertex, before: PlanarizationEdge?, after: PlanarizationEdge?): Boolean {
        return if (hasSameUnderlying(before, after)) {
            if (before!!.getDrawDirectionFrom(v) !== reverse(
                    after!!.getDrawDirectionFrom(
                        v
                    )
                )
            ) {
                true
            } else false
        } else true
    }

    private fun hasSameUnderlying(before: PlanarizationEdge?, after: PlanarizationEdge?): Boolean {
        return (before!!.getDiagramElements().keys
                == after!!.getDiagramElements().keys)
    }
}