package org.kite9.diagram.visualization.planarization

import org.kite9.diagram.model.Diagram

/**
 * Creates an initial [Planarization] of the graph, upon which transforms are applied.
 *
 * @author robmoffat
 */
interface PlanarizationBuilder {
    /**
     * Planarization of a hierarchically contained arrangement of [Connected]s
     */
    fun planarize(c: Diagram?): Planarization?
}