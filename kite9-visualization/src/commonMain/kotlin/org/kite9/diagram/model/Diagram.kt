package org.kite9.diagram.model

/**
 * A diagram is basically a container which manages a number of connections between elements
 * contained within it.
 *
 * @author robmoffat
 */
interface Diagram : Rectangular {

    fun getConnectionsFor(c: Connected): Collection<Connection>

}