package org.kite9.diagram.model

import org.kite9.diagram.model.position.End
import org.kite9.diagram.model.style.HorizontalAlignment
import org.kite9.diagram.model.style.LabelPlacement
import org.kite9.diagram.model.style.VerticalAlignment

/**
 * DiagramElement to contain a label for an edge, container or diagram.
 * Labels take up space on the diagram, so they have to be processed in the *orthogonalization* phase.
 * however they don't have connections so they are excluded from the Planarization phase.
 */
interface Label : Rectangular {

    fun isConnectionLabel(): Boolean

    /**
     * If this is a connection label, returns the end of the connection that it is for.
     */
    fun getEnd(): End?

    fun getLabelPlacement(): LabelPlacement?

}