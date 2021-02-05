package org.kite9.diagram.visualization.planarization

import org.kite9.diagram.logging.LogicException


class PlanarizationException(string: String?, var planarization: Planarization, e: Exception?) :
    LogicException(string, e) {

    companion object {
        private const val serialVersionUID = 3493217216660687045L
    }
}