package org.kite9.diagram.visualization.compaction.align;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.model.AlignedRectangular;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.model.style.HorizontalAlignment;
import org.kite9.diagram.model.style.VerticalAlignment;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.segment.Segment;
import org.kite9.diagram.visualization.compaction.slideable.SegmentSlackOptimisation;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;

/**
 * Basic approach:
 *  - The container is fixed, but we want to make the amount of space between each element, or element and container, even.
 *  - To do this, proceed from the outside to the inside elements.  
 *  - For the outside elements, work out the minimum amount of slack they have, divide it by the number of gaps (i.e. remaining elements + 1)
 *  - Reduce the outside gaps by this amount.
 *  - Move to the next level of analysis.
 */
public class CenteringAligner implements Aligner, Logable {

	protected Kite9Log log = new Kite9Log(this);
	
	@Override
	public void alignFor(Container co, Set<Rectangular> des, Compaction c, boolean horizontal) {
		SegmentSlackOptimisation sso = horizontal ?  c.getVerticalSegmentSlackOptimisation() : c.getHorizontalSegmentSlackOptimisation();
		log.send("Center Align: "+(horizontal ? "horiz" : "vert"), des);
		
		Layout l = co.getLayout();
		boolean inLine = Layout.isHorizontal(l) == horizontal;
		
		if (inLine) {
			List<Slideable<Segment>> matches = findRelevantSlideables(des, sso);
			
			if (matches.size() != des.size() * 2) {
				throw new Kite9ProcessingException("Was expecting this to be true");
			}
			
			log.send("Slideables to Align: ", matches);
			
			for (int i = 0; i < Math.ceil(des.size() / 2d); i++) {
				Slideable<Segment> leftD = matches.get(i * 2);
				Slideable<Segment> rightD = matches.get(matches.size() - (i * 2) - 1);
				centerSlideables(leftD, rightD, des.size() - (i*2));
			}
		} else {
			// do one-at-a-time
			for (Rectangular r : des) {
				OPair<Slideable<Segment>> rSlideables = sso.getSlideablesFor(r);
				centerSlideables(rSlideables.getA(), rSlideables.getB(), 1);
			}
		}
	}

	public List<Slideable<Segment>> findRelevantSlideables(Set<Rectangular> des, SegmentSlackOptimisation sso) {
		return sso.getAllSlideables().stream()
			.filter(s -> s.getUnderlying().hasUnderlying(des))
			.sorted((a, b) -> Integer.compare(a.getMinimumPosition(), b.getMinimumPosition()))
			.collect(Collectors.toList());
	}

	private void centerSlideables(Slideable<Segment> left, Slideable<Segment> right, int elementCount) {
		int leftSlack = minSlack(left);
		int rightSlack = minSlack(right);
		int slackToUse = Math.min(leftSlack, rightSlack);
		
		if (slackToUse == 0) {
			return;
		}
		
		slackToUse = slackToUse / (elementCount + 1);
		try {
			int leftFixed = left.getMinimumPosition() + slackToUse;
			int rightFixed = right.getMaximumPosition() - slackToUse;
			
			left.setMinimumPosition(leftFixed);
			right.setMaximumPosition(rightFixed);
			
			// remove all remaining slack
			right.setMinimumPosition(right.getMaximumPosition());
			left.setMaximumPosition(left.getMinimumPosition());
		} catch (Exception e) {
			throw new Kite9ProcessingException("Could not set center align constraint: ", e);
		}
	}
	
	private int minSlack(Slideable<Segment> l) {
		Integer leftMin = l.getMinimumPosition();
		Integer leftMax = l.getMaximumPosition();
		int leftSlack = leftMax - leftMin;
		return leftSlack;
	}

	@Override
	public boolean willAlign(Rectangular de, boolean horizontal) {
		if (!(de instanceof AlignedRectangular)) {
			return false;
		}
		if (horizontal) {
			return (((AlignedRectangular) de).getHorizontalAlignment() == HorizontalAlignment.CENTER);
		} else {
			return (((AlignedRectangular) de).getVerticalAlignment() == VerticalAlignment.CENTER);
		}
	}

	@Override
	public String getPrefix() {
		return "CNRA";
	}

	@Override
	public boolean isLoggingEnabled() {
		return true;
	}

}
