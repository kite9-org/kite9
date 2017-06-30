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
public class LeftRightAlignmentCompactionStep extends AbstractAlignmentCompactionStep {

	public LeftRightAlignmentCompactionStep(CompleteDisplayer cd) {
		super(cd);
	}

	
	
	@Override
	protected void alignConnectionSegment(Segment s, Compaction c) {
		alignSegment(s);
	}

	@Override
	protected void alignRectangular(Rectangular de, Compaction c) {
		alignRectangularAxis(de, c.getHorizontalSegmentSlackOptimisation());
		alignRectangularAxis(de, c.getVerticalSegmentSlackOptimisation());
	}

	private void alignRectangularAxis(Rectangular de, SegmentSlackOptimisation sso) {
		OPair<Slideable<Segment>> oss = sso.getSlideablesFor(de);
		alignSegment(oss.getA().getUnderlying());
		alignSegment(oss.getB().getUnderlying());
	}



	private void alignSegment(Segment s) {
		Slideable<Segment> sl = s.getSlideable();
		if (s.getAlignStyle() == AlignStyle.MAX) {
			Integer max = sl.getMaximumPosition();
			sl.setMinimumPosition(max);
		} else if (s.getAlignStyle() == AlignStyle.MIN) {
			int min = sl.getMinimumPosition();
			sl.setMaximumPosition(min);
		}
	}

}
