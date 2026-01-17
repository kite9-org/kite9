package org.kite9.diagram.visualization.planarization.rhd.grouping

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.common.elements.factory.AbstractDiagramElement
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.Connection
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.PlacementPositioned
import org.kite9.diagram.model.Temporary
import org.kite9.diagram.model.position.RectangleRenderingInformation
import org.kite9.diagram.model.style.ConnectionsSeparation
import org.kite9.diagram.model.style.Placement

/**
 * This is created for containers that need to participate in grouping,
 * because they have either directed connections arriving at them or they are
 * involved in layouts, but they also have contents that are involved in
 * layouts too.
 */
class TemporaryContainerHub(val c: Container) : AbstractDiagramElement(c), PlacementPositioned, Connected, Temporary {

    val links = mutableListOf<Connection>()

    override fun getContainerPosition(d: Dimension): Placement {
        if (c is Connected) {
            val ca = c.getConnectionAlignment(d)
            if (ca is Placement) {
                return ca
            }
        }

        return Placement.NONE   // default, middle of the container
    }

    override fun getRenderingInformation(): RectangleRenderingInformation {
        TODO("Not yet implemented")
    }

    override fun getContainer(): Container {
        return c
    }

    override fun getID(): String {
        return c.getID()+"-hub"
    }

    override fun getLinks(): Collection<Connection> {
        return links
    }

    override fun getConnectionsSeparationApproach(): ConnectionsSeparation {
        if (c is Connected) {
            return c.getConnectionsSeparationApproach()
        } else {
            return ConnectionsSeparation.SAME_SIDE
        }
    }

    override fun getConnectionAlignment(d: Dimension): Placement {
        return Placement.NONE   // not used
    }


    override fun toString(): String {
        return "[hub-temporary: " + getID() + "]"

    }
}