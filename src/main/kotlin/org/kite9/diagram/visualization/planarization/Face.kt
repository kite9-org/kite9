package org.kite9.diagram.visualization.planarization

import org.kite9.diagram.common.algorithms.det.DetHashSet
import org.kite9.diagram.common.algorithms.det.Deterministic
import org.kite9.diagram.common.elements.edge.Edge
import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.logging.Table
import org.kite9.diagram.model.Rectangular
import java.util.*

/**
 *
 * Describes a face in the graph, which is an area bounded by edges.
 *
 *
 * A clockwise ordering of edges is an internal face, whereas an anti-clockwise ordering is
 * an external face.
 *
 *
 * Note that the order of the nodes in the corners and boundary are important, and should follow
 * each other.  The first element of boundary should have as it's vertices the first two attr of the corners, and
 * so on.  CheckFaceIntegrity aims to make sure this is the case.
 *
 *
 * Note that since an edge can only have a face either side of it, an edge can only appear a maximum of twice
 * in the boundary.  There is no upper limit on the number of times a vertex can appear in the boundary.
 *
 *
 *
 * Faces can contain other faces, when there is content within the face that is not connected to
 * it's border.  This means that there is a hierarchy within the face system.  Any face that is contained
 * within another face must be an outer face (since it faces outwards), and it's container must be an inner
 * face.  Therefore, only inner faces may contain others.
 *
 * @author robmoffat
 */
class Face internal constructor(private val id: String, var pln: AbstractPlanarization) : Deterministic {
    private var boundary: MutableList<PlanarizationEdge> = ArrayList()
    private var corners: MutableList<Vertex> = ArrayList()
    var partOf: Rectangular? = null
    var isOuterFace = false
    override fun toString(): String {
        val out = StringBuffer(300)
        out.append(
            """
    [FACE: $id${if (isOuterFace) "outer" + (if (containedBy == null) "" else ", inside " + containedBy!!.id) else "inner"}
    
    """.trimIndent()
        )
        if (partOf != null) {
            out.append("  Part of: ")
            out.append(partOf)
            out.append("\n")
        }
        if (boundary.size == 0) {
            if (corners.size > 0) {
                out.append(" " + corners[0])
            }
        } else {
            val t = Table()
            t.addRow("Vertex", "Direction", "Contradicting", "Underlyings")
            for (i in 0 until vertexCount()) {
                val v = corners[i]
                val e = boundary[i]
                val d = e.getDrawDirectionFrom(v)
                val underlyings = e.getDiagramElements().keys
                val contradicting = Tools.isUnderlyingContradicting(e)
                t.addRow(v.toString(), d, if (contradicting) "C" else "", underlyings)
            }
            t.display(out)
        }
        return out.toString()
    }

    /**
     * This is a simple check to make sure that the boundary and list of corners reconciles ok.
     */
    fun checkFaceIntegrity(): Boolean {
        if (corners.size == 1 && boundary.size == 0) {
            return true
        }
        if (corners.size != boundary.size) {
            throw LogicException("Face: " + id + ": Faces need same numbers of edges and corners")
        }
        for (i in corners.indices) {
            val corner = corners[i]
            val otherEnd = corners[(i + 1) % corners.size]
            val e: Edge = boundary[i]
            if (!e.meets(corner)) {
                throw LogicException("Face: " + id + ": Edge should always meet the corner with same index: " + i + " " + corner + " " + e)
            }
            if (otherEnd !== e.otherEnd(corner)) {
                throw LogicException("Face: " + id + ": Edge doesn't meet the other end expected: " + i + " " + corner + " " + e + " expected: " + otherEnd)
            }
        }
        return true
    }

    operator fun contains(v: Vertex): Boolean {
        return corners.contains(v)
    }

    operator fun contains(e: Edge?): Boolean {
        return boundary.contains(e)
    }

    fun edgeIterator(): Iterable<PlanarizationEdge> {
        return boundary
    }

    fun cornerIterator(): Iterable<Vertex> {
        return corners
    }

    fun add(v: Vertex, e: PlanarizationEdge) {
        corners.add(v)
        boundary.add(e)
    }

    fun add(index: Int, v: Vertex, e: PlanarizationEdge) {
        corners.add(index, v)
        boundary.add(index, e)
    }

    fun remove(index: Int) {
        corners.removeAt(index)
        boundary.removeAt(index)
    }

    fun replaceEdge(e: PlanarizationEdge, with: PlanarizationEdge) {
        for (i in 0 until edgeCount()) {
            val current: Edge = boundary[i]
            if (current === e) {
                boundary[i] = with
            }
        }
    }

    fun vertexCount(): Int {
        return corners.size
    }

    fun edgeCount(): Int {
        return boundary.size
    }

    fun getCorner(i: Int): Vertex {
        var i = i
        i = normalize(i)
        return corners[i]
    }

    private fun normalize(i: Int): Int {
        return if (boundary.size == 0) {
            0
        } else if (i >= boundary.size) {
            i % boundary.size
        } else if (i < 0) {
            (i + boundary.size) % boundary.size
        } else {
            i
        }
    }

    fun getBoundary(i: Int): PlanarizationEdge {
        var i = i
        i = normalize(i)
        return boundary[i]
    }

    fun reset(boundary: MutableList<PlanarizationEdge>, corners: MutableList<Vertex>) {
        this.boundary = boundary
        this.corners = corners
        checkFaceIntegrity()
    }

    /**
     * Works out the list of vertices for the edges
     */
    fun reset(boundary: MutableList<PlanarizationEdge>) {
        var vList = createVertexList(boundary[0].getFrom(), boundary)
        if (vList == null) {
            vList = createVertexList(boundary[0].getTo(), boundary)
        }
        if (vList == null) {
            throw LogicException("Boundary is not consistent: $boundary")
        }
        reset(boundary, vList)
    }

    private fun createVertexList(from: Vertex, boundary: List<PlanarizationEdge>): MutableList<Vertex>? {
        var from = from
        val out = ArrayList<Vertex>(boundary.size)
        var b = 0
        do {
            out.add(from)
            val nextEdge: Edge = boundary[b]
            if (nextEdge.meets(from)) {
                from = nextEdge.otherEnd(from)
                b++
            } else {
                return null
            }
        } while (b < boundary.size)
        return out
    }

    /**
     * For two edges that follow each other on a face boundary, returns true
     * if to follows from in the order of the face.
     */
    fun toAfterFrom(from: Edge, to: Edge, first: Vertex): Boolean {
        if (!(from.meets(first) && to.meets(first))) {
            // this ensures the calling code has got the right common vertex
            throw LogicException("logic Error")
        }
        val ind = indexOf(first, to)
        if (ind == -1) return false
        val before: Edge = getBoundary(ind - 1)
        val after: Edge = getBoundary(ind + 1)
        return if (before === from) {
            true
        } else if (after === from) {
            false
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(obj: Any?): Boolean {
        return if (obj is Face) id == obj.id else false
    }

    /**
     * A specific vertex being followed by a specific edge occurs uniquely in a face.
     * This is because each edge can only occur twice in a face, and the ends of the edge
     * must be different.
     *
     * This returns the index of that occurrence or -1 if there is no occurrence.
     */
    fun indexOf(v: Vertex, e: Edge): Int {
        for (i in boundary.indices) {
            if (boundary[i] === e && corners[i] === v) {
                return i
            }
        }
        return -1
    }

    /**
     * Returns a list of 0, 1 or 2 integers giving the positions of the edge in the face.
     */
    fun indexOf(e: Edge): List<Int> {
        val out: MutableList<Int> = ArrayList()
        for (i in boundary.indices) {
            if (boundary[i] === e) {
                out.add(i)
            }
        }
        return out
    }

    val edgesCopy: List<PlanarizationEdge>
        get() = ArrayList(boundary)

    /**
     * Returns a list of integers giving the positions of the vertex in the face.
     */
    fun indexOf(v: Vertex): List<Int> {
        val out: MutableList<Int> = ArrayList()
        for (i in corners.indices) {
            if (corners[i] === v) {
                out.add(i)
            }
        }
        return out
    }

    /**
     * Return the number of times vertex v is visited by the face.
     */
    fun occurrences(v: Vertex): Int {
        var out = 0
        for (c in corners) {
            if (c === v) {
                out++
            }
        }
        return out
    }

    /**
     * Return the number of times edge e is visited by the face.
     */
    fun occurrences(e: Edge): Int {
        var out = 0
        for (ee in boundary) {
            if (ee === e) {
                out++
            }
        }
        return out
    }

    /**
     * Splits the current face in two from the given indexes.  The new face is returned, this face is modified.
     */
    fun split(start: Int, end: Int, repair: PlanarizationEdge): Face {
        val f2 = pln.createFace()
        f2.boundary = getRotatingSubset(boundary, end, start, false)
        boundary = getRotatingSubset(boundary, start, end, false)
        f2.corners = getRotatingSubset(corners, end, start, true)
        corners = getRotatingSubset(corners, start, end, true)
        f2.boundary.add(repair)
        boundary.add(repair)
        return f2
    }

    /**
     * Splits the current face in two using the given edge.  The new face is returned, this face is modified.
     * This is used where an edge appears twice in the face already.
     */
    fun split(e: Edge): Face {
        val newBoundary: MutableList<PlanarizationEdge> = ArrayList()
        val newCorners: MutableList<Vertex> = ArrayList()
        val f2 = pln.createFace()
        var addToNew = false
        for (i in corners.indices) {
            if (boundary[i] === e) {
                addToNew = !addToNew
            } else {
                if (addToNew) {
                    f2.boundary.add(boundary[i])
                    f2.corners.add(corners[i])
                } else {
                    newBoundary.add(boundary[i])
                    newCorners.add(corners[i])
                }
            }
        }
        corners = newCorners
        boundary = newBoundary

        // only problem now is where we end up with a single isolated vertex to handle
        val from = e.getFrom()
        val to = e.getTo()
        val fromFace = if (corners.contains(from)) this else if (f2.corners.contains(from)) f2 else null
        val toFace = if (corners.contains(to)) this else if (f2.corners.contains(to)) f2 else null
        if (fromFace == null) {
            if (corners.size == 0) {
                corners.add(from)
            } else {
                f2.corners.add(from)
            }
        }
        if (toFace == null) {
            if (corners.size == 0) {
                corners.add(to)
            } else {
                f2.corners.add(to)
            }
        }
        checkFaceIntegrity()
        f2.checkFaceIntegrity()
        return f2
    }

    override fun getID(): String {
        return id
    }

    val containedFaces: Set<Face> = DetHashSet()
    var containedBy: Face? = null
    fun setCorner(i: Int, newVertex: Vertex) {
        corners[i] = newVertex
    }

    fun size(): Int {
        return boundary.size
    }

    companion object {
        fun getCommonVertex(thisDart: Edge, nextDart: Edge): Vertex {
            return if (thisDart.meets(nextDart.getFrom())) {
                nextDart.getFrom()
            } else if (thisDart.meets(nextDart.getTo())) {
                nextDart.getTo()
            } else {
                throw LogicException("Logic Error")
            }
        }

        /**
         * if from==to, you get nothing back
         * includes from, excludes to
         */
        fun <X> getRotatingSubset(l: List<X>, from: Int, to: Int, inclusive: Boolean): MutableList<X> {
            var from = from
            var to = to
            from = (from + l.size) % l.size
            to = (to + l.size) % l.size
            val out: MutableList<X> = ArrayList()
            var i = from
            while (i != to) {
                out.add(l[i])
                i = i + 1
                if (i == l.size) {
                    i = 0
                }
            }
            if (inclusive) {
                out.add(l[i])
            }
            return out
        }
    }
}