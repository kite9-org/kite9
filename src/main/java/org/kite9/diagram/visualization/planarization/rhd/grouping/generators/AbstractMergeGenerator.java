package org.kite9.diagram.visualization.planarization.rhd.grouping.generators;

import org.kite9.diagram.adl.Container;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Group;
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.BasicMergeState;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;

public abstract class AbstractMergeGenerator implements MergeGenerator, Logable  {

	protected GroupPhase gp;
	protected BasicMergeState ms;
	protected GeneratorBasedGroupingStrategy grouper;
	protected Kite9Log log = new Kite9Log(this);

	public AbstractMergeGenerator(GroupPhase gp, BasicMergeState ms, GeneratorBasedGroupingStrategy grouper) {
		super();
		this.gp = gp;
		this.ms = ms;
		this.grouper = grouper;
	}
	
	public void addMergeOption(Group g1, Group g2, Group alignedGroup, Direction alignedSide) {
		grouper.addMergeOption(g1, g2, alignedGroup, alignedSide, getMyBestPriority(), ms);
	}

	@Override
	public String getPrefix() {
		return "MG  ";
	}

	@Override
	public boolean isLoggingEnabled() {
		return true;
	}
	
	protected abstract int getMyBestPriority();
	
	@Override
	public void containerIsLive(Container c) {
	}
	
	protected abstract String getCode();
}