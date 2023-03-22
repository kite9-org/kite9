package org.kite9.diagram.visualization.planarization.mgt

import org.kite9.diagram.common.BiDirectional
import org.kite9.diagram.common.Collections
import org.kite9.diagram.common.elements.edge.Edge
import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.*
import org.kite9.diagram.model.position.BasicDimension2D
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.planarization.Tools.Companion.isUnderlyingContradicting
import org.kite9.diagram.visualization.planarization.ordering.VertexEdgeOrdering
import org.kite9.diagram.visualization.planarization.rhd.RHDPlanarizationImpl
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashSet
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class MGTPlanarizationImpl(
    d: Diagram,
    inputVertexOrder: List<Vertex>,
    inputUninsertedConnections: Collection<BiDirectional<Connected>>,
    containerOrderingMap: Map<Container, List<Connected>>
) : RHDPlanarizationImpl(
    d, containerOrderingMap
), MGTPlanarization {

    override fun toString(): String {
        return if (faces.size == 0) {
            getTextualRepresentation(null).toString()
        } else {
            super.toString()
        }
    }

    override val vertexOrder = inputVertexOrder.toMutableList()
    override val uninsertedConnections = inputUninsertedConnections.toMutableList()
    private val aboveSet: MutableSet<PlanarizationEdge> = LinkedHashSet()
    private val belowSet: MutableSet<PlanarizationEdge> = LinkedHashSet()
    override val aboveLineEdges: Set<PlanarizationEdge>
        get() = aboveSet
    override val belowLineEdges: Set<PlanarizationEdge>
        get() = belowSet

    private val aboveForwardLinks: MutableList<MutableList<PlanarizationEdge>>
    private val aboveBackwardLinks: MutableList<MutableList<PlanarizationEdge>>
    private val belowForwardLinks: MutableList<MutableList<PlanarizationEdge>>
    private val belowBackwardLinks: MutableList<MutableList<PlanarizationEdge>>

    override fun getAboveForwardLinks(v: Vertex): MutableList<PlanarizationEdge> {
        return aboveForwardLinks[getVertexIndex(v)]
    }

    override fun getAboveBackwardLinks(v: Vertex): MutableList<PlanarizationEdge> {
        return aboveBackwardLinks[getVertexIndex(v)]
    }

    override fun getBelowForwardLinks(v: Vertex): MutableList<PlanarizationEdge> {
        return belowForwardLinks[getVertexIndex(v)]
    }

    override fun getBelowBackwardLinks(v: Vertex): MutableList<PlanarizationEdge> {
        return belowBackwardLinks[getVertexIndex(v)]
    }

    override val allEdges: List<Edge>
        get() {
            val out: MutableList<Edge> = ArrayList()
            out.addAll(aboveSet)
            out.addAll(belowSet)
            return out
        }

    /**
     * This does not output crossing edges or on line edges
     */
    fun getTextualRepresentation(highlight: Set<DiagramElement>?): TextualRepresentation {
        val tr = TextualRepresentation()

        // set up vertex positions in the textual rep.
        var voi = 0
        var lastPos = 0
        for (b in vertexOrder) {
            tr.positions[b] = BasicDimension2D(lastPos.toDouble(), 0.0)
            val name = b.getID() + "[" + voi++ + "]"
            lastPos += name.length + 2
        }
        tr.length = lastPos
        voi = 0
        for (v in vertexOrder) {
            tr.outputString(0, tr.positions[v]!!.x().toInt(), v.getID() + "[" + voi++ + "]")
        }
        var nestings: MutableMap<PlanarizationEdge?, Int?> = HashMap(aboveSet.size * 2)
        for (e in aboveSet) {
            val from = e.getFrom()
            val to = e.getTo()
            var fromi = tr.positions[from]!!.x().toInt()
            var toi = tr.positions[to]!!.x().toInt()
            if (fromi > toi) {
                val temp = toi
                toi = fromi
                fromi = temp
            }
            val height = getNestings(e, aboveSet, nestings) + 1
            val hl = isPartOf(highlight, e)
            tr.vLine(-1, fromi, -height, hl)
            tr.vLine(-1, toi, -height, hl)
            tr.hLine(-height - 1, fromi + 1, toi - 1, hl)
        }
        nestings = HashMap(belowSet.size * 2)
        for (e in belowSet) {
            val from = e.getFrom()
            val to = e.getTo()
            var fromi = tr.positions[from]!!.x().toInt()
            var toi = tr.positions[to]!!.x().toInt()
            if (fromi > toi) {
                val temp = toi
                toi = fromi
                fromi = temp
            }
            val height = getNestings(e, belowSet, nestings) + 1
            val hl = isPartOf(highlight, e)
            tr.vLine(1, fromi, height, hl)
            tr.vLine(1, toi, height, hl)
            tr.hLine(height + 1, fromi + 1, toi - 1, hl)
        }
        return tr
    }

    private fun isPartOf(highlight: Set<DiagramElement>?, e: Edge): Boolean {
        if (highlight == null) {
            return false
        }
        for (de in highlight) {
            if (e.isPartOf(de)) {
                return true
            }
        }
        return false
    }

    fun getNestings(
        e: PlanarizationEdge,
        aboveSet: Set<PlanarizationEdge>,
        nestCache: MutableMap<PlanarizationEdge?, Int?>
    ): Int {
        val out = nestCache[e]
        if (out != null) {
            return out
        }
        var nestings = 0
        var from: Int = vertexOrder.indexOf(e.getFrom())
        var to: Int = vertexOrder.indexOf(e.getTo())
        if (from > to) {
            val temp = to
            to = from
            from = temp
        }
        for (edge in aboveSet) {
            if (e !== edge) {
                var fromi: Int = vertexOrder.indexOf(edge.getFrom())
                var toi: Int = vertexOrder.indexOf(edge.getTo())
                if (fromi > toi) {
                    val temp = toi
                    toi = fromi
                    fromi = temp
                }
                if (fromi >= from && toi <= to && !(fromi == from && toi == to)) {
                    nestings = max(nestings, getNestings(edge, aboveSet, nestCache) + 1)
                }
            }
        }
        nestCache[e] = nestings
        return nestings
    }

    override fun removeEdge(cross: Edge) {
        val found = aboveSet.remove(cross) || belowSet.remove(cross)
        val fromvi = getVertexIndex(cross.getFrom())
        val tovi = getVertexIndex(cross.getTo())
        if (fromvi == -1 || tovi == -1) {
            throw LogicException()
        }

        // remove one end
        val found1 = aboveForwardLinks[fromvi].remove(cross) ||
                belowForwardLinks[fromvi].remove(cross) ||
                aboveBackwardLinks[fromvi].remove(cross) ||
                belowBackwardLinks[fromvi].remove(cross)
        val found2 = aboveForwardLinks[tovi].remove(cross) ||
                belowForwardLinks[tovi].remove(cross) ||
                aboveBackwardLinks[tovi].remove(cross) ||
                belowBackwardLinks[tovi].remove(cross)
        if (!found) {
            throw LogicException()
        }
    }

    private val vertexIndex: MutableMap<Vertex, Int> = mutableMapOf()

    private fun createVertexIndexMap() {
        vertexIndex.clear()
        var i = 0
        for (vertex in vertexOrder) {
            vertexIndex[vertex] = i
            i++
        }
    }

    override fun addVertexToOrder(i: Int, insert: Vertex) {
        vertexOrder.add(i + 1, insert)
        aboveBackwardLinks.add(i + 1, mutableListOf())
        aboveForwardLinks.add(i + 1, mutableListOf())
        belowBackwardLinks.add(i + 1, mutableListOf())
        belowForwardLinks.add(i + 1, mutableListOf())
        createVertexIndexMap()
    }

    override fun getVertexIndex(v: Vertex): Int {
        val i = vertexIndex!![v]
        return i ?: -1
    }

    override fun isAdjacency(edge: Edge): Boolean {
        val v1 = getVertexIndex(edge.getFrom())
        val v2 = getVertexIndex(edge.getTo())
        return abs(v1 - v2) <= 1
    }

    override fun crosses(edge: Edge, above: Boolean): Boolean {
        val v1 = edge.getFrom()
        val v2 = edge.getTo()
        val i1 = getVertexIndex(v1)
        val i2 = getVertexIndex(v2)
        return crosses(i1.toFloat(), i2.toFloat(), above)
    }

    override fun crosses(i1: Float, i2: Float, above: Boolean): Boolean {
        for (edge2 in if (above) aboveSet else belowSet) {
            if (crosses(i1, i2, getVertexIndex(edge2.getFrom()).toFloat(), getVertexIndex(edge2.getTo()).toFloat())) {
                return true
            }
        }
        return false
    }

    protected fun crosses(i1: Float, i2: Float, j1: Float, j2: Float): Boolean {
        if (i1 == j1 || i1 == j2 || i2 == j1 || i2 == j2) return false
        return if (within(i1, j1, j2) != within(i2, j1, j2)) {
            true
        } else false
    }

    protected fun within(i1: Float, j1: Float, j2: Float): Boolean {
        val ja = min(j1, j2)
        val jb = max(j1, j2)
        return i1 > ja && i1 < jb
    }

    override fun addEdge(edge: PlanarizationEdge, above: Boolean, outsideOf: PlanarizationEdge?) {
        var outsideOf = outsideOf
        var fromi = getVertexIndex(edge.getFrom())
        var toi = getVertexIndex(edge.getTo())
        if (fromi > toi) {
            val temp = toi
            toi = fromi
            fromi = temp
        }
        if (outsideOf != null && (!outsideOf.meets(edge.getFrom()) || !outsideOf.meets(edge.getTo()))) {
            outsideOf = null
        }
        if (above) {
            aboveSet.add(edge)
            orderedInsert(aboveForwardLinks[fromi], edge, outsideOf)
            orderedInsert(aboveBackwardLinks[toi], edge, outsideOf)
        } else {
            belowSet.add(edge)
            orderedInsert(belowForwardLinks[fromi], edge, outsideOf)
            orderedInsert(belowBackwardLinks[toi], edge, outsideOf)
        }
        val contradicting = isUnderlyingContradicting(edge)
        checkEdgeOrdering(edge.getFrom(), edge.getDrawDirectionFrom(edge.getFrom()), contradicting)
        checkEdgeOrdering(edge.getTo(), edge.getDrawDirectionFrom(edge.getTo()), contradicting)
        checkOrderingAround(edge.getFrom())
        checkOrderingAround(edge.getTo())
    }

    private fun checkEdgeOrdering(from: Vertex, direction: Direction?, contradicting: Boolean) {
        // first, fix the vertex ordering
        var eo = edgeOrderings[from] as VertexEdgeOrdering?
        if (eo == null) {
            eo = MGTVertexEdgeOrdering(this, from)
            edgeOrderings[from] = eo
            eo.addEdgeDirection(direction, contradicting)
        } else {
            eo.addEdgeDirection(direction, contradicting)
            eo.changed()
        }
    }

    private fun checkOrderingAround(from: Vertex) {
        val byQuad: MutableList<Edge> = ArrayList()
        val af: MutableList<PlanarizationEdge> = getAboveForwardLinks(from)
        val bf: MutableList<PlanarizationEdge> = getBelowForwardLinks(from)
        val bb: MutableList<PlanarizationEdge> = getBelowBackwardLinks(from)
        val ab: MutableList<PlanarizationEdge> = getAboveBackwardLinks(from)
        val c1: MutableList<PlanarizationEdge> = af.asReversed()
        byQuad.addAll(c1)
        byQuad.addAll(bf)
        val c2 = bb.asReversed()
        byQuad.addAll(c2)
        byQuad.addAll(ab)
        val cmp = edgeOrderings[from]!!.getEdgesAsList()
        val i = byQuad.indexOf(cmp[0])
        Collections.rotate(byQuad, -i)
        if (cmp != byQuad) {
            throw LogicException("Collections not same: \n$cmp\n$byQuad")
        }
    }

    override fun getFirstEdgeAfterPlanarizationLine(from: Vertex, forward: Boolean, above: Boolean): PlanarizationEdge {
        val outsideOf: PlanarizationEdge
        val clockwise = above != forward
        var q = getQuadrantFor(above, forward)
        var edgeSet = getEdgeSetByQuadrant(q, from)
        while (edgeSet.size == 0) {
            val ord = (q.ordinal + (if (clockwise) -1 else 1) + 4) % 4
            q = Quadrant.values()[ord]
            edgeSet = getEdgeSetByQuadrant(q, from)
        }
        outsideOf = if (q.clockwise == clockwise) edgeSet[0] else edgeSet[edgeSet.size - 1]
        return outsideOf
    }

    private fun getQuadrantFor(above: Boolean, forward: Boolean): Quadrant {
        return if (above) {
            if (forward) Quadrant.ABOVE_FORWARD else Quadrant.ABOVE_BACKWARD
        } else {
            if (forward) Quadrant.BELOW_FORWARD else Quadrant.BELOW_BACKWARD
        }
    }

    enum class Quadrant(val clockwise: Boolean) {
        ABOVE_FORWARD(false), ABOVE_BACKWARD(true), BELOW_BACKWARD(false), BELOW_FORWARD(true);
    }

    fun getEdgeSetByQuadrant(quadrant: Quadrant?, v: Vertex): List<PlanarizationEdge> {
        return when (quadrant) {
            Quadrant.ABOVE_BACKWARD -> getAboveBackwardLinks(v)
            Quadrant.ABOVE_FORWARD -> getAboveForwardLinks(v)
            Quadrant.BELOW_BACKWARD -> getBelowBackwardLinks(v)
            Quadrant.BELOW_FORWARD -> getBelowForwardLinks(v)
            else -> getBelowForwardLinks(v)
        }
    }

    private fun orderedInsert(
        abl: MutableList<PlanarizationEdge>,
        e: PlanarizationEdge,
        outsideOf: PlanarizationEdge?
    ) {
        val o1d = edgeSpan(e)
        val li = abl.listIterator()
        while (li.hasNext()) {
            val n = li.next()
            val o2d = edgeSpan(n)
            if (o2d > o1d || o2d == o1d && outsideOf == null) {
                li.previous()
                li.add(e)
                return
            }
            if (o2d == o1d && outsideOf === n) {
                li.add(e)
                return
            }
        }
        li.add(e)
    }

    private fun edgeSpan(e: Edge): Int {
        return abs(getVertexIndex(e.getFrom()) - getVertexIndex(e.getTo()))
    }

    init {
        createVertexIndexMap()
        aboveForwardLinks = ArrayList(vertexOrder.size)
        aboveBackwardLinks = ArrayList(vertexOrder.size)
        belowForwardLinks = ArrayList(vertexOrder.size)
        belowBackwardLinks = ArrayList(vertexOrder.size)
        for (i in vertexOrder.indices) {
            aboveBackwardLinks.add(mutableListOf())
            aboveForwardLinks.add(mutableListOf())
            belowBackwardLinks.add(mutableListOf())
            belowForwardLinks.add(mutableListOf())
        }
    }
}