package org.kite9.diagram.visualization.compaction.slideable;

import org.kite9.diagram.common.algorithms.so.AlignStyle;
import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.visualization.compaction.AbstractCompactionStep;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.display.CompleteDisplayer;

/**
 * Handles the following compaction optimisations:
 * <ul>
 * <li>Moving sections of edges up or down, left or right if they have a preference</li>
 * </ul>
 * 
 * @author robmoffat
 * 
 */
public class LeftRightAlignmentCompactionStep extends AbstractCompactionStep {

	public LeftRightAlignmentCompactionStep(CompleteDisplayer cd) {
		super(cd);
	}

	public void optimise(Compaction c, SegmentSlackOptimisation xo, SegmentSlackOptimisation yo) {
		applyContentRule(xo);
		applyContentRule(yo);
	}

	private void applyContentRule(SegmentSlackOptimisation so) {
		for (Slideable s : so.getAllSlideables()) {
			if (s.getAlignStyle() == AlignStyle.RIGHT) {
				Slideable from = s;
				Slideable to = s.getAlignTo();

				if (to == null) {
					// aligning a single slideable (e.g. edge length)
					int slack1 = from.getMaximumPosition() - from.getMinimumPosition();
					if (slack1 > 0) {
						int amt = from.getMinimumPosition() + slack1;
						from.increaseMinimum(amt);
						from.decreaseMaximum(amt);
					}
				}
			} else if (s.getAlignStyle() == AlignStyle.LEFT) {
				Slideable from = s;
				Slideable to = s.getAlignTo();

				if (to == null) {
					// aligning a single slideable (e.g. edge length)
					int slack1 = from.getMaximumPosition() - from.getMinimumPosition();
					if (slack1 > 0) {
						int amt = from.getMinimumPosition();
						from.increaseMinimum(amt);
						from.decreaseMaximum(amt);
					}
				}
			} 
		}

	}
}
