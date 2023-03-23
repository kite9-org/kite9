package org.kite9.diagram.visualization.planarization.mgt.router

import org.kite9.diagram.common.elements.vertex.Vertex

/**
 * This class holds the location of the ssp node, which can be either outsideEdge or below or arriving at any given vertex.
 */
data class Location(val p: AbstractRouteFinder.Place?, val trailEndVertex: Int, val vertex: Vertex) {

    override fun toString(): String {
        return "LOC[p=$p, vertex=$trailEndVertex,v=$trailEndVertex]"
    }

}