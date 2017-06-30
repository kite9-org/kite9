package org.kite9.diagram.visualization.compaction.slideable;

import org.kite9.diagram.common.algorithms.so.AlignStyle;
import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.segment.Segment;
import org.kite9.diagram.visualization.display.CompleteDisplayer;

/**
 * Handles the following compaction optimisations:
 * <ul>
 * <li>Moving sections of edges up or down, left or right if they have a preference</li>
 * </ul>
 * 
 * This means we can do 
 * 
 * @author robmoffat
 * 
 */
public class CenteringAlignmentCompactionStep extends AbstractAlignmentCompactionStep {

	public CenteringAlignmentCompactionStep(CompleteDisplayer cd) {
		super(cd);
	}

	
	
	@Override
	protected void alignConnectionSegment(Segment s, Compaction c) {
	}

	@Override
	protected void alignRectangular(Rectangular de, Compaction c) {
		alignRectangularAxis(de, c.getHorizontalSegmentSlackOptimisation());
		alignRectangularAxis(de, c.getVerticalSegmentSlackOptimisation());
	}

	private void alignRectangularAxis(Rectangular de, SegmentSlackOptimisation sso) {
		OPair<Slideable<Segment>> oss = sso.getSlideablesFor(de);
		
		Slideable<Segment> left = oss.getA();
		Slideable<Segment> right = oss.getB();
		if ((left.getUnderlying().getAlignStyle() == AlignStyle.CENTER) && 
		 (right.getUnderlying().getAlignStyle() == AlignStyle.CENTER)) {
			Integer leftMin = left.getMinimumPosition();
			Integer rightMax = right.getMaximumPosition();
			int leftSlack = left.getMaximumPosition() - leftMin;
			int rightSlack = rightMax - leftMin;
			
			int slackToUse = Math.min(leftSlack, rightSlack);
			left.
		 }
		
	}



	private void alignSegment(Segment s) {
		Slideable<Segment> sl = s.getSlideable();
		if (s.getAlignStyle() == AlignStyle.CENTER) {
			Integer max = sl.getMaximumPosition();
			sl.setMinimumPosition(max);
		} else if (s.getAlignStyle() == AlignStyle.LEFT) {
			int min = sl.getMinimumPosition();
			sl.setMaximumPosition(min);
		}
	}

}
