package org.kite9.diagram.visualization.planarization

import org.kite9.diagram.common.elements.edge.Edge
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.model.Diagram
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.Dimension2D
import org.kite9.diagram.visualization.planarization.ordering.EdgeOrdering

/**
 * Contains methods for:
 *
 *  * outputting the planarization as text
 *  * storing the faces in the planarization
 *  * storing the order of edges around a vertex
 *  * storing the mapping of faces either side of an edge
 *
 *
 * @author moffatr
 */
abstract class AbstractPlanarization(override val diagram: Diagram) : Planarization {

    class TextualRepresentation {
        var positions: Map<Vertex, Dimension2D> = HashMap()
        var b: MutableMap<Int, CharArray?> = HashMap()
        var length = 0
        var minExtent = Int.MAX_VALUE
        var maxExtent = Int.MIN_VALUE
        fun hLine(row: Int, cols: Int, coll: Int, highlight: Boolean) {
            ensureRow(row)
            val increment = if (cols > coll) -1 else 1
            var i = cols
            while (i != coll) {
                setLinePart(row, i, true, highlight)
                i += increment
            }
            setLinePart(row, coll, true, highlight)
        }

        private fun setLinePart(row: Int, i: Int, horiz: Boolean, highlight: Boolean) {
            if (highlight || b[row]!![i] == ' ') {
                b[row]!![i] = if (highlight) if (horiz) '=' else '+' else if (horiz) '-' else '|'
            }
        }

        fun ensureRow(i: Int) {
            if (b[i] != null) return
            val row = CharArray(length) { ' ' }
            b[i] = row
            minExtent = Math.min(minExtent, i)
            maxExtent = Math.max(maxExtent, i)
        }

        fun vLine(rows: Int, col: Int, rowl: Int, highlight: Boolean) {
            val increment = if (rows > rowl) -1 else 1
            var i = rows
            while (i != rowl) {
                ensureRow(i)
                setLinePart(i, col, false, highlight)
                i += increment
            }
            ensureRow(rowl)
            setLinePart(rowl, col, false, highlight)
        }

        fun outputString(row: Int, col: Int, label: String) {
            ensureRow(row)
            for (i in 0 until label.length) {
                b[row]!![col + i] = label[i]
            }
        }

        override fun toString(): String {
            val out = StringBuilder(1000)
            out.append("\n")
            for (i in minExtent..maxExtent) {
                val cs = b[i]
                for (j in cs!!.indices) {
                    out.append(cs[j])
                }
                out.append('\n')
            }
            return out.toString()
        }
    }

    override val faces: MutableList<Face> = ArrayList()
    override var edgeFaceMap: MutableMap<Edge, MutableList<Face>> = HashMap()
    override var edgeOrderings: MutableMap<Vertex, EdgeOrdering> = HashMap()
    override var vertexFaceMap: MutableMap<Vertex, MutableList<Face>> = HashMap()

    var nextFaceId = 0

    /**
     * Creates a face for this planarization
     * @param group An object to indicate a logical grouping of faces.
     * @return
     */
    override fun createFace(): Face {
        val out = Face("" + nextFaceId++, this)
        faces.add(out)
        return out
    }

    override fun toString(): String {
        val out = StringBuilder(1000)
        out.append("PLANARIZATION[\n$allVertices\n")
        for (f in faces) {
            out.append(f.toString())
            out.append("\n")
        }
        return out.toString()
    }

    override val edgeMappings: MutableMap<DiagramElement, EdgeMapping> = HashMap()

    override val allVertices: MutableCollection<Vertex>
        get() = vertexFaceMap.keys
}