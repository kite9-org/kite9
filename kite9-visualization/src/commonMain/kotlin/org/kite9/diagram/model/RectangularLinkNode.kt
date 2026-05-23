package org.kite9.diagram.model

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.common.elements.factory.AbstractDiagramElement
import org.kite9.diagram.model.position.RectangleRenderingInformation
import org.kite9.diagram.model.position.RectangleRenderingInformationImpl
import org.kite9.diagram.model.style.ConnectionsSeparation
import org.kite9.diagram.model.style.MeasuredRectangularPosition

/**
 * This is created for rectangulars that need to participate in grouping,
 * because they have either directed connections arriving at them or they are
 * involved in layouts, but they also have contents that are involved in
 * layouts too.
 */
class RectangularLinkNode(val c: Rectangular, suffix: String, val gridPosition: Pair<Int, Int>? = null) : AbstractDiagramElement(c), PlacementPositioned, LinkNode, Temporary {

    val id = c.getID()+'-'+suffix

    val links = mutableListOf<Connection>()

    override fun getContainerPosition(d: Dimension): MeasuredRectangularPosition {
        if (c is AlignedRectangular) {
            val ca = c.getConnectionAlignment(d)
            if (ca is MeasuredRectangularPosition) {
                return ca
            }
        }

        return MeasuredRectangularPosition.NONE   // default, middle of the container
    }

    override fun getRenderingInformation(): RectangleRenderingInformation {
        return RectangleRenderingInformationImpl(null, null, false)
    }

    override fun getContainer(): Rectangular {
        return c
    }

    override fun getID(): String {
        return id
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

    override fun toString(): String {
        return "[hub-temporary: " + getID() + "]"

    }
}