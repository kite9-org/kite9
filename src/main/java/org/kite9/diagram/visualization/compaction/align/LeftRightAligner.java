package org.kite9.diagram.visualization.compaction.align;

import java.util.List;

import org.kite9.diagram.common.algorithms.so.AlignStyle;
import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.model.AlignedRectangular;
import org.kite9.diagram.model.style.HorizontalAlignment;
import org.kite9.diagram.model.style.VerticalAlignment;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.segment.Segment;
import org.kite9.diagram.visualization.compaction.slideable.SegmentSlackOptimisation;

/**
 * If you have contradictory alignments, (e.g. thing on left wants to align right, thing on right wants to align left)
 * then this is going to be inconsistent.  Works outside-to-in aligning stuff.
 */
public class LeftRightAligner implements Aligner {

	@Override
	public void alignRectangulars(List<AlignedRectangular> des, Compaction c, boolean horizontal) {
		SegmentSlackOptimisation sso = horizontal ? c.getVerticalSegmentSlackOptimisation() : c.getHorizontalSegmentSlackOptimisation();

		for (int i = 0; i < (int) Math.ceil(des.size() / 2d); i++) {
			AlignedRectangular bottom = des.get(i);
			AlignedRectangular top = des.get(des.size()-i-1);

			alignRectangular(bottom, sso);
			if (top!=bottom) {
				alignRectangular(top, sso);
			}
		}
	}

	private void alignRectangular(AlignedRectangular de, SegmentSlackOptimisation sso) {
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
	public boolean willAlign(AlignedRectangular de, boolean horizontal) {
		if (horizontal) {
			return de.getHorizontalAlignment() != HorizontalAlignment.CENTER;
		} else {
			return de.getVerticalAlignment() != VerticalAlignment.CENTER;
		}
	}

}
