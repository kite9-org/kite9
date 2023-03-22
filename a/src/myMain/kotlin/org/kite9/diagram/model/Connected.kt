package org.kite9.diagram.model

import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.RectangleRenderingInformation
import org.kite9.diagram.model.style.Placement
import org.kite9.diagram.model.style.ConnectionsSeparation

interface Connected : DiagramElement {

    override fun getRenderingInformation(): RectangleRenderingInformation

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

    /**
     * In the case of single connections on a side, returns how that connection
     * should meet the side.
     */
    fun getConnectionAlignment(side: Direction): Placement

}