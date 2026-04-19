package org.kite9.diagram.visualization.planarization.rhd

import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.visualization.planarization.rhd.grouping.GroupLinkNode
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.LeafGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.merge.MergeOption

interface GroupBuilder {

    fun buildInitialGroups()

    /**
     * Leaf Groups represent a flat plane of objects to lay out on the diagram.  This means that
     * if you want to lay out a container and also some of its contents, the container must be given
     * ports, which are therefore at the same "level" as the contents.
     */
    fun createLeafGroup(gln: GroupLinkNode, ord: Rectangular) : LeafGroup

    /**
     * We combine leaf groups together in a compound hierarchy.
     */
    fun createCompoundGroup(a: Group, b: Group, treatAsLeaf: Boolean, mo: MergeOption?, size: Int = a.size + b.size) : CompoundGroup

}