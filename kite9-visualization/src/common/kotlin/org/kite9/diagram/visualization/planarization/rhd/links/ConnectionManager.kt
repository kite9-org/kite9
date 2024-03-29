package org.kite9.diagram.visualization.planarization.rhd.links

import org.kite9.diagram.common.BiDirectional
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.ConnectedRectangular
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group

interface ConnectionManager : MutableCollection<BiDirectional<Connected>> {

    fun handleLinks(g: Group)

    fun hasContradictions(): Boolean

}