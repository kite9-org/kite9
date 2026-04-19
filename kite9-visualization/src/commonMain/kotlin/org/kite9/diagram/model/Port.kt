package org.kite9.diagram.model

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.style.Placement
import org.kite9.diagram.visualization.planarization.rhd.grouping.GroupLinkNode

interface Port : GroupLinkNode, PlacementPositioned {

    fun getPortDirection(): Direction

    override fun getParent() : Rectangular
}