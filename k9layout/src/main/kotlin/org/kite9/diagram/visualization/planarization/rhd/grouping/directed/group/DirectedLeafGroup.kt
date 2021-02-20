package org.kite9.diagram.visualization.planarization.rhd.grouping.directed.group

import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.Container
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.AbstractLeafGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.merge.DirectedMergeState

class DirectedLeafGroup(contained: Connected?,
                        container: Container?,
                        groupNumber: Int,
                        hc: Int,
                        log: Kite9Log,
                        bs: DirectedMergeState)
    : AbstractLeafGroup(contained, container, groupNumber, hc) {

    override val axis : DirectedGroupAxis = DirectedGroupAxis(log,  this)

    override val linkManager : DirectedLinkManager = DirectedLinkManager(bs, this)

}