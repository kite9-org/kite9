package org.kite9.diagram.visualization.planarization.transform

import org.kite9.diagram.visualization.planarization.Planarization

/**
 * Allows for further transformations to the planarization after it has been constructed.
 *
 * @author robmoffat
 */
interface PlanarizationTransform {

    fun transform(pln: Planarization)
}