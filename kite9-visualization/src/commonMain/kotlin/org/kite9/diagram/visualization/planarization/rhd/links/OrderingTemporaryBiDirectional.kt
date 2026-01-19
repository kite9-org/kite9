package org.kite9.diagram.visualization.planarization.rhd.links

import org.kite9.diagram.common.elements.AbstractBiDirectional
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.ConnectedRectangular
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Temporary
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.RenderingInformation

/**
 * Used for enforcing container ordering.
 *
 * @author robmoffat
 */
class OrderingTemporaryBiDirectional(private val f: Connected,
                                     private val t: Connected,
                                     private val dd: Direction,
                                     private val c: Container) : AbstractBiDirectional<Connected>(), Temporary {

    private val id = f.getID() + ":"+t.getID();

    override fun getFrom(): Connected {
        return f
    }

    override fun getTo(): Connected {
        return t
    }

    override fun getDrawDirection(): Direction {
        return dd
    }

    override fun getID(): String {
        return id
    }

    override fun getParent(): DiagramElement? {
        return null
    }

    override fun getRenderingInformation(): RenderingInformation {
        throw LogicException("Temporary elements shouldn't be rendered")
    }

    override fun getDepth(): Int {
        return 0
    }

    override fun getContainer(): Container? {
        return null
    }

    override fun compareTo(other: DiagramElement): Int {
        throw LogicException("Not used")
    }


}