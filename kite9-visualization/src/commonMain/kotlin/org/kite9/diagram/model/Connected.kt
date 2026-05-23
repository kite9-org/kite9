package org.kite9.diagram.model

import org.kite9.diagram.model.style.ConnectionsSeparation

interface Connected : Positioned {

    /**
     * Returns an unmodifiable collection of links
     */
    fun getLinks(): Collection<Connection>

    private fun firstConnectionTo(c: Connected): Connection? {
        for (link in getLinks()) {
            if (link.meets(c)) {
                return link
            }
        }
        return null
    }

    /**
     * Means that there exists a connection with this object at one end and c
     * at the other.
     */
    fun isConnectedDirectlyTo(c: Connected): Boolean {
        return firstConnectionTo(c) != null
    }

    fun getConnectionsSeparationApproach(): ConnectionsSeparation

}