package org.kite9.diagram.model

import org.kite9.diagram.model.SizedRectangular
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.End

/**
 * Describes what's at the end of a [Connection].
 *
 * Terminators don't get involved in the usual compaction process (like Decals).
 * They can overlap Connecteds and Connections that they are part of.
 *
 * @author robmoffat
 */
interface Terminator : SizedRectangular {
    /**
     * Amount of length along the axis of the link that the terminator will take up.
     */
    fun getReservedLength(): Double

    /**
     * The part of the connection, from the end inwards, that doesn't need to be
     * drawn because the marker will draw it instead.
     */
    fun getMarkerReserve(): Double

    /**
     * This is used for making like-terminators collect around elements which have
     * [ConnectionsSeparation]
     */
    fun styleMatches(t2: Terminator): Boolean

    /**
     * Gives the side of the Connected element the terminator should be placed on.
     */
    fun getArrivalSide(): Direction

    /**
     * Returns the connection that this is a terminator for.
     */
    fun getConnection(): Connection

    /**
     * Returns which end of the parent link this is for.
     */
    fun getEnd(): End
}