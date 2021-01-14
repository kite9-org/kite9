package org.kite9.diagram.visualization.planarization.rhd.grouping.generators;


import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Group;
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.BasicMergeState;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.AbstractRuleBasedGroupingStrategy;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.DirectedLinkManager;

/**
 * Generates linked merge options for undirected links.
 * 
 * @author robmoffat
 *
 */
public class ContainerUndirectedLinkedMergeGenerator extends AbstractMergeGenerator {

	public ContainerUndirectedLinkedMergeGenerator(GroupPhase gp, BasicMergeState ms, GeneratorBasedGroupingStrategy grouper) {
		super(gp, ms, grouper);
	}

	public void generate(final Group group) {
		log.send(log.go() ? null : "Generating "+getCode()+" options for "+group);

		int mask = DirectedLinkManager.createMask(null, false, true, (Direction) null);
		
		for (Group destinationGroup : group.getLinkManager().subsetGroup(mask)) {
			addMergeOption(group, destinationGroup, null, null);
		}
	}

	@Override
	public int getMyBestPriority() {
		return AbstractRuleBasedGroupingStrategy.UNDIRECTED_LINKED;
	}
	
	@Override
	public String getCode() {
		return "ContainerUndirected";
	}
}
