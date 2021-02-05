package org.kite9.diagram.visualization.orthogonalization

import org.kite9.diagram.visualization.planarization.Planarization

/**
 * Creates the orthogonal representation from the Planarization.
 *
 * @author robmoffat
 */
interface Orthogonalizer {

    fun createOrthogonalization(pln: Planarization): Orthogonalization
}