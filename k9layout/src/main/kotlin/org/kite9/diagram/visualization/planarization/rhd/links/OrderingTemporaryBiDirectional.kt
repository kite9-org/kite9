package org.kite9.diagram.visualization.planarization.rhd.links

import org.kite9.diagram.common.elements.AbstractBiDirectional
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.Temporary
import org.kite9.diagram.model.position.Direction

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



}