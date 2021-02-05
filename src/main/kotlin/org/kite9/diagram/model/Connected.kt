package org.kite9.diagram.model

import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.style.ConnectionAlignment
import org.kite9.diagram.model.style.ConnectionsSeparation

/**
 * A diagram element which is consumes a rectangular area of space, and
 * potentially has [Connection]s that link to other [Connected] items within the diagram.
 *
 * Unlike the label, it is therefore involved in the Planarization phase.
 *
 * @author robmoffat
 */
interface Connected : Rectangular {
    /**
     * Returns an unmodifiable collection of links
     */
    fun getLinks(): Collection<Connection>

    /**
     * Means that there exists a connection with this object at one end and c
     * at the other.
     */
    fun isConnectedDirectlyTo(c: Connected): Boolean

    /**
     * Returns the connection between this object and c.
     */
    fun getConnectionTo(c: Connected): Connection?
    fun getConnectionsSeparationApproach(): ConnectionsSeparation

    /**
     * The minimum distance between two links on any side of the Connected.
     */
    fun getLinkGutter(): Double

    /**
     * The minimum distance from the start of a link and the corner of this connected.
     */
    fun getLinkInset(): Double

    /**
     * In the case of single connections on a side, returns how that connection
     * should meet the side.
     */
    fun getConnectionAlignment(side: Direction): ConnectionAlignment
}