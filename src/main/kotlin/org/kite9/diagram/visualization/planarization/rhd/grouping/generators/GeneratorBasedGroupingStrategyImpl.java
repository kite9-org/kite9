package org.kite9.diagram.visualization.planarization.rhd.grouping.generators;

import java.util.ArrayList;
import java.util.List;

import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.CompoundGroup;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Group;
import org.kite9.diagram.visualization.planarization.rhd.grouping.GroupResult;
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.AbstractGroupingStrategy;
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.BasicMergeState;
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.MergeKey;
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.MergeOption;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.AxisHandlingGroupingStrategy;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.DirectedGroupAxis;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.DirectedMergeState;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.PriorityRule;
import org.kite9.diagram.visualization.planarization.rhd.grouping.rules.AlignedDirectedPriorityRule;
import org.kite9.diagram.visualization.planarization.rhd.grouping.rules.NeighbourDirectedPriorityRule;
import org.kite9.diagram.visualization.planarization.rhd.grouping.rules.UndirectedPriorityRule;
import org.kite9.diagram.visualization.planarization.rhd.links.ContradictionHandler;

/**
 * This contains the mechanism for creating merge options from generators.
 * 
 * The general approach is to generate as few merge options as possible.  This is done by asking each
 * generator to create legal merge options.  Should a generator create a legal option, we stop 
 * and process the merges from it.
 * 
 * Generat
 * 
 * @author robmoffat
 *
 */
public class GeneratorBasedGroupingStrategyImpl extends AxisHandlingGroupingStrategy implements GeneratorBasedGroupingStrategy {

	public GeneratorBasedGroupingStrategyImpl(ContradictionHandler ch) {
		super(new GeneratorMergeState(ch));
	}

	public void addMergeOption(Group g1, Group g2, Group alignedGroup, Direction alignedSide, int bestPriority, BasicMergeState ms) {
		MergeKey mk = new MergeKey(g1, g2);
		MergeOption best = ms.getBestOption(mk);
		
		if ((best != null) && (best.getPriority()<=bestPriority)) {
			// we already have this option with at least as good a priority.
			return;
		}
		
		int p = canGroupsMerge(g1, g2, ms, alignedGroup, alignedSide);
		
		if (p != AbstractGroupingStrategy.INVALID_MERGE) {
			MergeOption mo = new MergeOption(g1, g2, ms.nextMergeOptionNumber(), p, alignedGroup, alignedSide, ms);
			mo.calculateMergeOptionMetrics(ms);
			boolean added = ms.addOption(mo);
			if (added) {
				log.send(log.go() ? null: "Added Merge Option: " + mo);
			}
		} 
	}
	
	public int addMergeOption(MergeOption mo, int bestPriority, BasicMergeState ms) {
		MergeKey mk = mo.mk;
		MergeOption best = ms.getBestOption(mk);
		
		if ((best != null) && (best.getPriority()<=bestPriority)) {
			// we already have this option with at least as good a priority.
			return best.getPriority();
		}
		
		Change c = updateMergeOption(mo, ms);
		if (c == Change.DISCARD) {
			return INVALID_MERGE;
		}
		
		int p = canGroupsMerge(mk.getA(), mk.getB(), ms, mo.alignedGroup, mo.alignedDirection);
		
		if (p != AbstractGroupingStrategy.INVALID_MERGE) {
			mo.resetPriority(ms, p);
			mo.calculateMergeOptionMetrics(ms);
			boolean added = ms.addOption(mo);
			if (added) {
				log.send(log.go() ? null: "Added Merge Option: " + mo);
			}
		} 
		
		return p;
	}
	
	/**
	 * Add MergeOptions to the queue for a given group.
	 * 
	 * @param optionMap
	 */
	public void createMergeOptions(GroupPhase gp, BasicMergeState ms) {
		GeneratorMergeState gms = (GeneratorMergeState) ms;
		Group next = gms.nextLiveGroup();
		while (next != null) {
			log.send("Merge options for:"+next);
			for (MergeGenerator strat : gms.generators) {
				strat.generate(next);
			}	
			next = gms.nextLiveGroup();
		}
	}
	
	public GroupResult group(GroupPhase gp) {
		int capacity = gp.groupCount;
		int containers = gp.containerCount;
		ms.initialise(capacity, containers, log);
		setupMergeState(ms, gp);

		preMergeInitialisation(gp, ms);

		while (ms.groupsCount() > 1) {			
			createMergeOptions(gp, ms);
					
			MergeOption mo;
			try {
				mo = ms.nextMergeOption();
				Change c = updateMergeOption(mo, ms);
				
				if ((c == Change.CHANGED) || (c==Change.NO_CHANGE)) {
					int p = canGroupsMerge(mo.mk.getA(), mo.mk.getB(), ms, mo.alignedGroup, mo.alignedDirection);
					if (p!= AbstractGroupingStrategy.INVALID_MERGE) {
						if ((p != mo.getPriority()) || (c == Change.CHANGED)) {
							// poke it back in to use in desperation
							mo.resetPriority(ms, p);
							mo.calculateMergeOptionMetrics(ms);
							ms.addOption(mo);
						} else {
							if (p == ILLEGAL_PRIORITY) {
								log.error("Inserting with Illegal: "+mo);
							}
							
							performMerge(gp, ms, mo);
						}
					}
				}
				
			} catch (RuntimeException e) {
				log.send("Groups:", ms.groups());
				throw e;
			}
		}

		return ms;
	}

	enum Change { NO_CHANGE, CHANGED, DISCARD };
	
	/**
	 * This is called when a merge option changes because groups within it have already
	 * been merged.
	 */
	private Change updateMergeOption(MergeOption mo, BasicMergeState ms) {
		Group a = getWorkingGroup(mo.mk.getA(), ms);
		Group b = getWorkingGroup(mo.mk.getB(), ms);
		Group alignedGroup = getWorkingGroup(mo.alignedGroup, ms);
		if ((a!=b) && (a!=alignedGroup) && (b!=alignedGroup)) {
			if ((a != mo.mk.getA()) || (b != mo.mk.getB()) || (alignedGroup!=mo.alignedGroup)) {
				MergeKey newKey = new MergeKey(a, b);
				mo.mk = newKey;
				mo.alignedGroup = alignedGroup;
				return Change.CHANGED;
			} else {
				return Change.NO_CHANGE;
			}
		}
		
		return Change.DISCARD;
	}

	protected void introduceCombinedGroup(GroupPhase gp, BasicMergeState ms, CompoundGroup combined) {
		combined.log(log);
		ms.addLiveGroup(combined);
	}


	public boolean compatibleMerge(Group a, Group b) {
		return DirectedGroupAxis.compatibleNeighbour(a, b);
	}
	
	protected void setupMergeState(BasicMergeState bms, GroupPhase gp) {
		GeneratorMergeState ms = (GeneratorMergeState) bms;
		List<MergeGenerator> generators = new ArrayList<MergeGenerator>();
		
		// axis merges take priority over everything else
		generators.add(new AxisSingleMergeGenerator(gp, ms, this));
		generators.add(new AxisAlignedMergeGenerator(gp, ms, this));
		generators.add(new AxisNeighbourMergeGenerator(gp, ms, this));
		
		// perpendicular, undirected, in-container merges
		generators.add(new ContainerUndirectedLinkedMergeGenerator(gp, ms, this));
		generators.add(new ContainerUndirectedAlignedMergeGenerator(gp, ms, this));
		generators.add(new ContainerUndirectedNeighbourMergeGenerator(gp, ms, this));
		
		// perpendicular, directed, in & out of container merges
		generators.add(new PerpendicularAlignedMergeGenerator(gp, ms, this));
		generators.add(new PerpendicularDirectedMergeGenerator(gp, ms, this));
		
		ms.generators = generators;
		
		
		// order is significant
		List<PriorityRule> rules = new ArrayList<PriorityRule>();
		
		// axis merges first
		rules.add(new NeighbourDirectedPriorityRule(true));
		rules.add(new AlignedDirectedPriorityRule(true));
		
		// undirected merges
		rules.add(new UndirectedPriorityRule());
		
		// perpendicular merges
		rules.add(new NeighbourDirectedPriorityRule(false));
		rules.add(new AlignedDirectedPriorityRule(false));

		ms.rules = rules;
	}
	
	@Override
	protected List<PriorityRule> getRules(DirectedMergeState ms) {
		return ((GeneratorMergeState)ms).rules;
	}

	@Override
	protected void startContainerMerge(BasicMergeState ms, Container c) {
		super.startContainerMerge(ms, c);
		for (MergeGenerator gen : ((GeneratorMergeState)ms).generators) {
			gen.containerIsLive(c);
		}
	}
	
	
}
