package org.kite9.diagram.visualization.planarization.rhd.grouping.generators;

import java.util.Collections;
import java.util.Set;

import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Group;
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.BasicMergeState;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.AbstractRuleBasedGroupingStrategy;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.DirectedGroupAxis;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.DirectedLinkManager;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.MergePlane;
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkProcessor;

/**
 * Looks for aligned merges, where an aligned merge is a pair of groups sharing
 * a common neighbour. Aligned merges should be in-container.
 * 
 * @author robmoffat
 * 
 */
public class ContainerUndirectedAlignedMergeGenerator extends AbstractAlignedMergeGenerator {

	private static final Set<Direction> NULL = Collections.singleton(null);

	public ContainerUndirectedAlignedMergeGenerator(GroupPhase gp, BasicMergeState ms, GeneratorBasedGroupingStrategy grouper) {
		super(gp, ms, grouper, true);
	}

	@Override
	protected int getMyBestPriority() {
		return AbstractRuleBasedGroupingStrategy.UNDIRECTED_ALIGNED;
	}

	@Override
	protected String getCode() {
		return "ContainerUndirectedAligned";
	}

	@Override
	protected void processPossibleAligningGroups(Group g, Direction d, LinkProcessor lp) {
		g.processAllLeavingLinks(true, g.getLinkManager().allMask(), lp);
	}
	

	@Override
	protected Set<Direction> getAlignmentDirections(Group g1) {
		return NULL;
	}

	@Override
	protected void processAlignedGroupsInAxis(Group alignedGroup, BasicMergeState ms, DirectedGroupAxis axis,
			MergePlane mp, Direction d, LinkProcessor lp) {
		int mask = DirectedLinkManager.createMask(mp,  false , false, d);
		alignedGroup.processAllLeavingLinks(true, mask, lp);
	}
	
}
