package org.kite9.diagram.visualization.compaction.align;

import java.util.Set;

import org.kite9.diagram.common.algorithms.so.AlignStyle;
import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.model.AlignedRectangular;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.style.HorizontalAlignment;
import org.kite9.diagram.model.style.VerticalAlignment;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.segment.Segment;
import org.kite9.diagram.visualization.compaction.slideable.SegmentSlackOptimisation;

/**
 * If you have contradictory alignments, (e.g. thing on left wants to align right, thing on right wants to align left)
 * then this is going to be inconsistent. 
 */
public class LeftRightAligner implements Aligner {

	@Override
	public void alignFor(Container co, Set<? extends Rectangular> des, Compaction c, boolean horizontal) {
		SegmentSlackOptimisation sso = horizontal ? c.getVerticalSegmentSlackOptimisation() : c.getHorizontalSegmentSlackOptimisation();

		for (Rectangular r : des) {
			alignRectangular(r, sso);
		}
	}

	private void alignRectangular(Rectangular de, SegmentSlackOptimisation sso) {
		OPair<Slideable<Segment>> oss = sso.getSlideablesFor(de);
		
		if (oss != null) {
			alignSegment(oss.getA().getUnderlying());
			alignSegment(oss.getB().getUnderlying());
		}
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

	@Override
	public boolean willAlign(Rectangular de, boolean horizontal) {
		if (!(de instanceof AlignedRectangular)) {
			return false;
		}
		if (horizontal) {
			return (((AlignedRectangular) de).getHorizontalAlignment() == HorizontalAlignment.LEFT) ||
					(((AlignedRectangular) de).getHorizontalAlignment() == HorizontalAlignment.RIGHT);
		} else {
			return (((AlignedRectangular) de).getVerticalAlignment() == VerticalAlignment.TOP) ||
					(((AlignedRectangular) de).getVerticalAlignment() == VerticalAlignment.BOTTOM);
		}
	}

}
