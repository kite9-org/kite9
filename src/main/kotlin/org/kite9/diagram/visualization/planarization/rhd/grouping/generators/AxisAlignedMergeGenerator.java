package org.kite9.diagram.visualization.planarization.rhd.grouping.generators;

import java.util.Collection;
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
import org.kite9.framework.logging.LogicException;

/**
 * Generates in-axis aligned merges. 
 * 
 * @author robmoffat
 *
 */
public class AxisAlignedMergeGenerator extends AbstractAlignedMergeGenerator {

	public static final Set<Direction> UP_DOWN = EnumSet.of(Direction.UP, Direction.DOWN);
	public static final Set<Direction> LEFT_RIGHT = EnumSet.of(Direction.LEFT, Direction.RIGHT);
	public static final Set<Direction> ALL = EnumSet.allOf(Direction.class);
	
	public AxisAlignedMergeGenerator(GroupPhase gp, BasicMergeState ms, GeneratorBasedGroupingStrategy grouper) {
		super(gp, ms, grouper, false);
	}

	@Override
	protected int getMyBestPriority() {
		return AbstractRuleBasedGroupingStrategy.AXIS_ALIGNED;
	}

	@Override
	protected String getCode() {
		return "InAxisBuddy";
	}


	@Override
	protected void processPossibleAligningGroups(Group g1, Direction d, LinkProcessor lp) {
		int mask = DirectedLinkManager.createMask(null, false, false, d);	
		g1.getLinkManager().processAllLeavingLinks(true, mask, lp);
	}

	@Override
	protected Set<Direction> getAlignmentDirections(Group g1) {
		MergePlane state = DirectedGroupAxis.getState(g1);
		switch (state) {
		case UNKNOWN:
			return ALL;
		case X_FIRST_MERGE:
			return LEFT_RIGHT;
		case Y_FIRST_MERGE:
			return UP_DOWN;
		}
		
		throw new LogicException("Unknown state: "+state);
	}
	
	@Override
	protected void processAlignedGroupsInAxis(Group alignedGroup, BasicMergeState ms, DirectedGroupAxis axis,
			MergePlane mp, Direction d, LinkProcessor lp) {

		Collection<Group> alignedGroups = getAlignedGroups(alignedGroup, axis, mp, d);
		for (Group g : alignedGroups) {
			lp.process(alignedGroup, g, null);
		}
	}

	protected Collection<Group> getAlignedGroups(Group g, DirectedGroupAxis axis, MergePlane mp, Direction d) {
		int mask = DirectedLinkManager.createMask(mp, true, false, d);
		return g.getLinkManager().subsetGroup(mask);
	}
}
