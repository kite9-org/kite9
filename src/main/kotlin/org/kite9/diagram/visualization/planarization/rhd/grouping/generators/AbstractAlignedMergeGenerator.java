package org.kite9.diagram.visualization.planarization.rhd.grouping.generators;

import java.util.Set;

import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Group;
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.BasicMergeState;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.DirectedGroupAxis;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.MergePlane;
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkDetail;
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkProcessor;

/**
 * Generates merge options where the group passed in is a member of the alignedGroup, g1, or g2 trio.
 * Sadly, this involves a lot more iteration than a g1 + g2 duo.
 * 
 * @author robmoffat
 * 
 */
public abstract class AbstractAlignedMergeGenerator extends AbstractWaitingContainerMergeGenerator {

	public AbstractAlignedMergeGenerator(GroupPhase gp, BasicMergeState ms, GeneratorBasedGroupingStrategy grouper,
			boolean liveOnly) {
		super(gp, ms, grouper, liveOnly);
	}

	protected abstract Set<Direction> getAlignmentDirections(Group g1);

	protected abstract void processPossibleAligningGroups(Group g1, Direction d, LinkProcessor lp);

	public void generate(final Group poll) {
		log.send(log.go() ? null : "Generating "+getCode()+" options for "+poll);
		Set<Direction> alignmentDirections = getAlignmentDirections(poll);
		for (Direction d : alignmentDirections) {
			generateFromAlignedGroup(gp, poll, d, ms);
			generateFromG1Group(gp, poll, d, ms);
		}
	}

	protected void generateFromAlignedGroup(GroupPhase gp, final Group alignedGroup, final Direction d, final BasicMergeState ms) {
//		if (alignedGroup.getLinkCount() < 2)
//			return;

		final DirectedGroupAxis axis = DirectedGroupAxis.getType(alignedGroup);
		final MergePlane mp = DirectedGroupAxis.getState(alignedGroup);
		
		processAlignedGroupsInAxis(alignedGroup, ms, axis, mp, d, new LinkProcessor() {

			@Override
			public void process(Group originatingGroup, final Group g1w, LinkDetail ld) {
				final Group g1 = grouper.getWorkingGroup(g1w, ms);
				final MergePlane g1state = DirectedGroupAxis.getState(g1);
				
				if (ms.isLiveGroup(g1) && mp.matches(g1state)) {
				
					processAlignedGroupsInAxis(alignedGroup, ms, axis, mp, d, new LinkProcessor() {
						
						@Override
						public void process(Group originatingGroup, Group g2, LinkDetail ld) {
							if (g2.getGroupNumber() > g1.getGroupNumber()) {
								testAndAddAlignedTrio(g1, g2, alignedGroup, d, mp);
							}
						}
					});
				}
			}
		});
	}
	
	protected void generateFromG1Group(GroupPhase gp, final Group g1, final Direction d, final BasicMergeState ms) {
//		if (alignedGroup.getLinkCount() < 2)
//			return;

		final DirectedGroupAxis axis = DirectedGroupAxis.getType(g1);
		final MergePlane mp = DirectedGroupAxis.getState(g1);
		
		processAlignedGroupsInAxis(g1, ms, axis, mp, d, new LinkProcessor() {

			@Override
			public void process(Group originatingGroup, final Group alignedGroupw, LinkDetail ld) {
				final Group alignedGroup = grouper.getWorkingGroup(alignedGroupw, ms);
				final Direction rd = Direction.reverse(d);
				
			//	if (ms.isLiveGroup(alignedGroup) && mp.matches(mp)) {
				
					processAlignedGroupsInAxis(alignedGroup, ms, axis, mp, rd, new LinkProcessor() {
						
						@Override
						public void process(Group originatingGroup, Group g2, LinkDetail ld) {
							testAndAddAlignedTrio(g1, g2, alignedGroup, rd, mp);
						}
					});
			//	}
			}
		});
	}
	
	protected void testAndAddAlignedTrio(Group g1, Group g2, Group alignedGroup, Direction alignedSide, MergePlane mp) {
		g2 = grouper.getWorkingGroup(g2, ms);
		MergePlane g2state = DirectedGroupAxis.getState(g2);
		if ((g2 != g1) && (ms.isLiveGroup(g2)) && (mp.matches(g2state))) {
			addMergeOption(g1, g2, alignedGroup, alignedSide);
		}
	}

	

	protected abstract void processAlignedGroupsInAxis(final Group alignedGroup, final BasicMergeState ms,
			DirectedGroupAxis axis, MergePlane mp, Direction d, LinkProcessor lp);

	
	

}