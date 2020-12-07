package org.kite9.diagram.visualization.planarization

import org.kite9.diagram.model.Diagram

/**
 * Interface for creating a Planarization of a set of diagram attr suitable for further layout.
 *
 * @author robmoffat
 */
interface Planarizer {
    fun planarize(d: Diagram?): Planarization?
}