package org.kite9.diagram.visualization.orthogonalization.flow

import org.kite9.diagram.visualization.orthogonalization.Orthogonalization
import org.kite9.diagram.visualization.orthogonalization.OrthogonalizationImpl
import org.kite9.diagram.visualization.planarization.Planarization

/**
 * Converts an optimised flow graph and planarization into an orthogonal model.
 *
 * @author robmoffat
 */
interface OrthBuilder {

    fun build(pln: Planarization): Orthogonalization
}