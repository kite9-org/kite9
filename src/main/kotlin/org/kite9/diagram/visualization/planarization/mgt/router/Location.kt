package org.kite9.diagram.visualization.planarization.mgt.router

import org.kite9.diagram.common.elements.vertex.Vertex

/**
 * This class holds the location of the ssp node, which can be either outsideEdge or below or arriving at any given vertex.
 */
class Location(var p: AbstractRouteFinder.Place?, val trailEndVertex: Int, val vertex: Vertex) {

    override fun toString(): String {
        return "LOC[p=$p, vertex=$trailEndVertex,v=$trailEndVertex]"
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + if (p == null) 0 else p.hashCode()
        result = prime * result + trailEndVertex
        return result
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) return true
        if (obj == null) return false
        if (javaClass != obj.javaClass) return false
        val other = obj as Location
        if (p != other.p) return false
        return if (trailEndVertex != other.trailEndVertex) false else true
    }
}