package org.kite9.diagram.visualization.planarization.mgt.face

import org.kite9.diagram.visualization.planarization.mgt.MGTPlanarization

/**
 * Creates the dual graph of faces for a Planarization.
 * Labels one of the faces as an outer face.
 *
 * @author robmoffat
 */
interface FaceConstructor {

    fun createFaces(pl: MGTPlanarization)
}