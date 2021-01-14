package org.kite9.diagram.visualization.planarization.rhd.grouping.generators

import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.BasicMergeState
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.AbstractRuleBasedGroupingStrategy
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.DirectedLinkManager

/**
 * Generates linked merge options for undirected links.
 *
 * @author robmoffat
 */
class ContainerUndirectedLinkedMergeGenerator(
    gp: GroupPhase,
    ms: BasicMergeState,
    grouper: GeneratorBasedGroupingStrategy
) : AbstractMergeGenerator(
    gp, ms, grouper
) {
    override fun generate(group: GroupPhase.Group) {
        log.send(if (log.go()) null else "Generating " + getCode() + " options for " + group)
        val mask = DirectedLinkManager.createMask(null, false, true, null as Direction?)
        for (destinationGroup in group.linkManager.subsetGroup(mask)) {
            addMergeOption(group, destinationGroup, null, null)
        }
    }

    public override fun getMyBestPriority(): Int {
        return AbstractRuleBasedGroupingStrategy.UNDIRECTED_LINKED
    }

    override fun getCode(): String {
        return "ContainerUndirected"
    }
}