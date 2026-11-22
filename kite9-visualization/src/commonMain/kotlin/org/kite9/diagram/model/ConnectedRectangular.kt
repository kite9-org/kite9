package org.kite9.diagram.model

/**
 * A diagram element which is consumes a rectangular area of space, and
 * potentially has [Connection]s that link to other [ConnectedRectangular] items within the diagram.
 *
 * Unlike the label, it is therefore involved in the Planarization phase.
 *
 * @author robmoffat
 */
interface ConnectedRectangular : Rectangular, Connected {

    /**
     * The minimum distance between two links on any side of the Connected.
     */
    fun getLinkGutter(): Double

    /**
     * The minimum distance from the start of a link and the corner of this connected.
     */
    fun getLinkInset(): Double

}