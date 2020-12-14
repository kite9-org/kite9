package org.kite9.diagram.visualization.planarization.rhd

import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager

interface GroupBuilder {

    fun createAxis(): GroupAxis
    fun createLinkManager(): LinkManager

}