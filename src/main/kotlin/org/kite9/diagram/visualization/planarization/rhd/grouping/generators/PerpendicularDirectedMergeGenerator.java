package org.kite9.diagram.visualization.planarization.rhd.grouping.generators;

import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Group;
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.BasicMergeState;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.AbstractRuleBasedGroupingStrategy;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.DirectedGroupAxis;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.DirectedLinkManager;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.MergePlane;

/**
 * Generates nearest-neighbour merge options for perpendicular groups in axis.  These can span containers, 
 * and generally have the highest priority.
 * 
 * @author robmoffat
 *
 */
public class PerpendicularDirectedMergeGenerator extends AbstractMergeGenerator {

	public PerpendicularDirectedMergeGenerator(GroupPhase gp, BasicMergeState ms, 
			GeneratorBasedGroupingStrategy grouper) {
		super(gp, ms, grouper);
	}

	@Override
	public void generate(Group poll) {
		MergePlane state = DirectedGroupAxis.getState(poll);
		if ((state == MergePlane.X_FIRST_MERGE) || (state==MergePlane.UNKNOWN)) {
			// horizontal merges
			generateMergesInDirection(poll, getMs(), false, MergePlane.X_FIRST_MERGE);
		}
		
		if ((state == MergePlane.Y_FIRST_MERGE) || (state==MergePlane.UNKNOWN)) {
			// vertical merges
			generateMergesInDirection(poll, getMs(), true, MergePlane.Y_FIRST_MERGE);
		}
	}

	private void generateMergesInDirection(Group poll, BasicMergeState ms, boolean horizontal, MergePlane mp) {
		DirectedLinkManager dlm = (DirectedLinkManager) poll.getLinkManager();
		int mask = horizontal ?  DirectedLinkManager.createMask(mp, true, false, Direction.LEFT, Direction.RIGHT):
			DirectedLinkManager.createMask(mp, true, true, Direction.UP, Direction.DOWN);
		
		
		for (Group g : dlm.subsetGroup(mask)) {
			addMergeOption(poll, g, null, null);
		}
	}

	@Override
	protected int getMyBestPriority() {
		return AbstractRuleBasedGroupingStrategy.PERP_NEIGHBOUR;
	}

	@Override
	protected String getCode() {
		return "PerpendicularNeighbour";
	}

}
