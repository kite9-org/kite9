package org.kite9.diagram.visualization.planarization.rhd.links

import org.kite9.diagram.common.BiDirectional
import org.kite9.diagram.model.Connected
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase

interface ConnectionManager : MutableCollection<BiDirectional<Connected>> {

    fun handleLinks(g: GroupPhase.Group)

    fun hasContradictions(): Boolean

}