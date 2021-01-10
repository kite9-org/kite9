package org.kite9.diagram.visualization.orthogonalization

import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.position.Direction

/**
 * Stores details of a face made up of darts.  The darts form a perimeter to the face.
 * This is now extended so that if a face contains subgraphs, these are also held as dart-faces
 *
 *
 * @author robmoffat
 */
class DartFace(val id: Int, val outerFace: Boolean, val dartsInFace: List<DartDirection>, val partOf: Rectangular?) {

    data class DartDirection(val dart: Dart, val direction: Direction)

    override fun toString(): String {
        val containedByStr = if (containedBy == null) "-" else "" + containedBy!!.id
        return "DartFace: " + id + "-" + (if (outerFace) "outer, inside $containedByStr" else "inner") + ": " + dartsInFace.toString()
    }

    private var containedBy: DartFace? = null
    private val containing: MutableSet<DartFace> = HashSet()
    fun getContainedBy(): DartFace? {
        return containedBy
    }

    fun setContainedBy(containedBy: DartFace?) {
        if (!outerFace) {
            throw LogicException()
        }
        if (this.containedBy != null) {
            throw LogicException()
        }
        if (containedBy != null) {
            this.containedBy = containedBy
            this.containedBy!!.containing.add(this)
        }
    }

    val startVertex: Vertex
        get() {
            val d1 = dartsInFace[0]
            return if (d1.direction === d1.dart.getDrawDirection()) {
                d1.dart.getFrom()
            } else {
                d1.dart.getTo()
            }
        }
    val containedFaces: Set<DartFace>
        get() = containing

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + id
        result = prime * result + if (outerFace) 1231 else 1237
        return result
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) return true
        if (obj == null) return false
        if (javaClass != obj.javaClass) return false
        val other = obj as DartFace
        if (id != other.id) return false
        return if (outerFace != other.outerFace) false else true
    }

    companion object {
        private const val serialVersionUID = -4395910839686963521L
    }
}