package org.kite9.diagram.visualization.planarization

import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.model.Connection

/**
 * Utility functions for manipulating the Planarization
 *
 * @author robmoffat
 */
class Tools : Logable {
    var log = Kite9Log.instance(this)
    var elementNo = 0
    override val prefix: String
        get() = "PLNT"
    override val isLoggingEnabled: Boolean
        get() = true

    companion object {

		fun isConnectionContradicting(c: Connection): Boolean {
            val rri = c.getRenderingInformation()
            return rri.isContradicting
        }


		fun isConnectionRendered(c: Connection): Boolean {
            val rri = c.getRenderingInformation()
            return rri.rendered
        }


		fun setConnectionContradiction(c: Connection, contradicting: Boolean, rendering: Boolean) {
            val rri = c.getRenderingInformation()
            if (c.getDrawDirection() != null) {
                rri.isContradicting = contradicting
            }
            rri.rendered = rendering
        }
    }
}