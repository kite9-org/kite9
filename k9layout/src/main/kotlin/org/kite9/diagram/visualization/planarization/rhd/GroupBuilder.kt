package org.kite9.diagram.visualization.planarization.rhd

import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.Container
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.LeafGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.merge.MergeOption

interface GroupBuilder {

    fun buildInitialGroups()

    fun createLeafGroup(ord: Connected?, cnr: Container?) : LeafGroup

    fun createCompoundGroup(a: Group, b: Group, treatAsLeaf: Boolean, mo: MergeOption?, size: Int = a.size + b.size) : CompoundGroup

}