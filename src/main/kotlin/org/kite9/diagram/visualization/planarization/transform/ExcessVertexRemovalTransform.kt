package org.kite9.diagram.visualization.planarization.transform

import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.visualization.planarization.Planarization
import org.kite9.diagram.visualization.planarization.Tools

/**
 * This performs the following transformations:
 *
 *  1.  Remove excess start/end temporary vertices
 *  1.  Remove any dimensionless vertex with only two edges incident to it
 *
 *
 *
 * @author robmoffat
 */
class ExcessVertexRemovalTransform : PlanarizationTransform, Logable {

    override fun transform(pln: Planarization) {
        removeExcessVertices(pln)
    }

    private fun removeExcessVertices(pln: Planarization) {
        val t = Tools()
        val vertices: List<Any> = ArrayList<Any>(pln.edgeOrderings.keys)
        for (v in vertices) {
            if (v is Vertex) {
                t.checkRemoveVertex(pln, v)
            }
        }
    }

    override val prefix: String
        get() = "EVRT"
    override val isLoggingEnabled: Boolean
        get() = false
}