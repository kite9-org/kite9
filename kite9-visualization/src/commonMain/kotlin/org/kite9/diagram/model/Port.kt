package org.kite9.diagram.model

import org.kite9.diagram.model.position.Direction

interface Port : LinkNode, PlacementPositioned {

    fun getPortDirection(): Direction

    override fun getParent() : Rectangular
}