/**
 *
 */
package org.kite9.diagram.visualization.orthogonalization.flow.face

import org.kite9.diagram.common.elements.edge.Edge
import org.kite9.diagram.visualization.planarization.Face

/**
 * A route through faces used in the [ConstraintGroupGenerator] algorithm
 *
 * @author robmoffat
 */
data class Route(val face: Face, val _in: Int, val _out: Int, var rest: Route?) {

    fun size(): Int {
        return if (rest == null) {
            1
        } else {
            1 + rest!!.size()
        }
    }

    override fun toString(): String {
        return if (rest == null) {
            face.getBoundary(_out).toString() + "-" + face.getID() + "-" + face.getBoundary(_in).toString()
        } else {
            face.getBoundary(_out).toString() + "-" + face.getID() + "-" + rest.toString()
        }
    }

    fun containsFace(f: Face): Boolean {
        return face === f || rest != null && rest!!.containsFace(f)
    }

    val inEdge: Edge
        get() = face.getBoundary(_in)

    val outEdge: Edge
        get() = face.getBoundary(_out)



}