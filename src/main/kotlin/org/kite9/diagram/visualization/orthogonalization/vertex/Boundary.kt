package org.kite9.diagram.visualization.orthogonalization.vertex

import org.kite9.diagram.visualization.orthogonalization.vertex.ExternalVertex
import org.kite9.diagram.visualization.orthogonalization.DartFace.DartDirection

/**
 * Contains part of the overall vertex construction, between one incoming vertex and the next.
 *
 * @author robmoffat
 */
data class Boundary(val from: ExternalVertex, val to: ExternalVertex, val toInsert: List<DartDirection>) {

    override fun toString(): String {
        return "Boundary [from=$from, to=$to]"
    }

}