package org.kite9.diagram.visualization.compaction.slideable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kite9.diagram.common.algorithms.det.UnorderedSet;
import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.visualization.compaction.AbstractCompactionStep;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.Compactor;
import org.kite9.diagram.visualization.compaction.Embedding;
import org.kite9.diagram.visualization.display.CompleteDisplayer;

public class SlackSharingCompactionStep extends AbstractCompactionStep {

	public SlackSharingCompactionStep(CompleteDisplayer cd) {
		super(cd);
	}
//
//	@Override
//	public String getPrefix() {
//		return "SCOS";
//	}
//
//	@Override
//	public boolean isLoggingEnabled() {
//		return true;
//	}
//	
//	int nextSlackPoolId = 0;
//	
//	static class SlackPool implements Comparable<SlackPool>{
//		
//		int id;
//		
//		SlackPool parent;
//		
//		@Override
//		public int compareTo(SlackPool o) {
//			return new Integer(this.level).compareTo(o.level);
//		}
//
//		int level, slackAmount;
//		Slideable from;
//		List<Slideable> includesSlackItems = new LinkedList<Slideable>();
//
//		public SlackPool(int id, Slideable from, SlackPool parent, int slackAmount, Slideable s) {
//			super();
//			this.id = id;
//			this.parent = parent;
//			this.slackAmount = slackAmount;
//			this.level = (parent==null) ? 0 : (parent.level+1);
//			this.includesSlackItems.add(s);
//			this.from = from;
//		}
//
//		@Override
//		public String toString() {
//			return "SlackPool [id=" + id + ", parent=" + parent.id + ", level="
//					+ level + ", slackAmount=" + slackAmount + ", from=" + from
//					+ ", includesSlackItems=" + includesSlackItems + "]";
//		}
//
//		
//		
//	}
//
//	@Override
//	public void compact(Compaction c, Rectangular r, Compactor rc) {
//		shareSlack(c.getHorizontalSegmentSlackOptimisation(), c);
//		shareSlack(c.getVerticalSegmentSlackOptimisation(), c);
//	}
//
//	private void shareSlack(SegmentSlackOptimisation xo, Compaction c) {
//		
//		int capacity = xo.getAllSlideables().size()*2;
//		Map<Slideable, SlackPool> slackPoolMap = new HashMap<Slideable, SlackSharingCompactionStep.SlackPool>(capacity);
//		Set<SlackPool> slackPools = new UnorderedSet<SlackSharingCompactionStep.SlackPool>(capacity);
//		
//		for (Slideable s : xo.getAllSlideables()) {
//			int slack = s.getMaximumPosition() - s.getMinimumPosition();
//			
//			// ok, work through links to other elements
//			SlackPool lowestPool = null;
//			Slideable dueTo = null;
//			for (Slideable s2 :  s.getMinLeft()) {
//				SlackPool sp = slackPoolMap.get(s2);
//				if ((lowestPool == null) || (sp.slackAmount > lowestPool.slackAmount)) {
//					lowestPool = sp;
//					dueTo = s2;
//				}
//			}
//			
//			if (lowestPool == null) { 
//				SlackPool sp = new SlackPool(nextSlackPoolId++, dueTo, null, slack, s);
//				slackPoolMap.put(s, sp);
//				slackPools.add(sp);
//			} else if (lowestPool.slackAmount < slack) {
//				// slack increases
//				SlackPool sp = new SlackPool(nextSlackPoolId++, dueTo, lowestPool, slack, s);
//				slackPoolMap.put(s, sp);
//				slackPools.add(sp);
//			} else {
//				// slack decreases, so lowest pool has ended.  But, we need to have a slack pool for the new guy
//				while (lowestPool.slackAmount > slack) {
//					lowestPool = lowestPool.parent;
//				}
//				
//				slackPoolMap.put(s, lowestPool);
//				lowestPool.includesSlackItems.add(s);
//			}
//		}
//		
//		// order the pools by the amount of slack they have
//		List<SlackPool> out = new ArrayList<SlackSharingCompactionStep.SlackPool>(slackPools);
//		Collections.sort(out);
//		
//		for (SlackPool slackPool : out) {
//			Slideable right = slackPool.includesSlackItems.get(0);
//			Slideable left = slackPool.from;
//			
//			if (left != null) {
//				int leftPos = left.getMinimumPosition();
//				int rightPos = right.getMinimumPosition();
//				int offset = rightPos - leftPos;
//				
//				int leftSlack = left.getMaximumPosition() - left.getMinimumPosition();
//				int rightSlack = right.getMaximumPosition() - right.getMinimumPosition();
//				
//				int slackToApportion = rightSlack - leftSlack;
//				int moveAmount = offset + (slackToApportion /2);	
//
//				log.send("Centering Slackpool: "+slackPool+" by "+moveAmount);
//				if (moveAmount != 0) {
//					xo.ensureMinimumDistance(slackPool.from, slackPool.includesSlackItems.get(0), moveAmount, true);
//				}
//			}
//			
//		}
//	}

	@Override
	public void compact(Compaction c, Embedding e, Compactor rc) {
		// TODO Auto-generated method stub
		
	}
	
}
