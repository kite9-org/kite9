package org.kite9.diagram.visualization.planarization.rhd.grouping.generators;

import java.util.Collections;
import java.util.EnumSet;
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
import org.kite9.diagram.logging.LogicException;

/**
 * Looks for perpendicular aligned merges, where an aligned merge is a pair of groups sharing
 * a common neighbour, not necessarily in the same container.
 * 
 * @author robmoffat
 * 
 */
public class PerpendicularAlignedMergeGenerator extends AbstractAlignedMergeGenerator {

	private static final Set<Direction> LEFT_RIGHT = EnumSet.of(Direction.LEFT, Direction.RIGHT);
	private static final Set<Direction> UP_DOWN = EnumSet.of(Direction.UP, Direction.DOWN);
	private static final Set<Direction> NONE = Collections.emptySet();
	
	public PerpendicularAlignedMergeGenerator(GroupPhase gp, BasicMergeState ms, GeneratorBasedGroupingStrategy grouper) {
		super(gp, ms, grouper, false);
	}

	@Override
	protected int getMyBestPriority() {
		return AbstractRuleBasedGroupingStrategy.PERP_ALIGNED;
	}

	@Override
	public String getCode() {
		return "ContainerPerpAligned";
	}


	@Override
	protected void processPossibleAligningGroups(Group g, Direction d, LinkProcessor lp) {
		g.processAllLeavingLinks(true, g.getLinkManager().allMask(), lp);
	}
	

	@Override
	protected Set<Direction> getAlignmentDirections(Group g1) {
		MergePlane state = DirectedGroupAxis.getState(g1);
		switch (state) {
		case UNKNOWN:
			return NONE;
		case X_FIRST_MERGE:
			return UP_DOWN;
		case Y_FIRST_MERGE:
			return LEFT_RIGHT;
		}
		
		throw new LogicException("Unknown state: "+state);
	}

	@Override
	protected void processAlignedGroupsInAxis(Group alignedGroup, BasicMergeState ms, DirectedGroupAxis axis,
			MergePlane mp, Direction d, LinkProcessor lp) {
		int mask = DirectedLinkManager.createMask(mp,  true , false, d);
		alignedGroup.processAllLeavingLinks(true, mask, lp);
	}

}
