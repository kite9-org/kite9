package org.kite9.diagram.visualization.compaction.slideable;

import java.util.Set;

import org.kite9.diagram.common.algorithms.so.AlignStyle;
import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.segment.Segment;
import org.kite9.diagram.visualization.display.CompleteDisplayer;

/**
 * Handles the following compaction optimisations:
 * <ul>
 * <li>Moving sections of edges up or down, left or right if they have a preference</li>
 * </ul>

 * @author robmoffat
 * 
 */
public class CenteringAlignmentCompactionStep extends AbstractAlignmentCompactionStep {

	public CenteringAlignmentCompactionStep(CompleteDisplayer cd) {
		super(cd);
	}

	
	
	@Override
	protected void alignConnectionSegment(Segment s, Compaction c) {
		int balance = s.getAdjoiningSegmentBalance();
		int pos = 0;
		Slideable<Segment> slideable = s.getSlideable();
		if (balance == 0) {
			int slack = slideable.getMaximumPosition() - slideable.getMinimumPosition();
			pos = slideable.getMinimumPosition() + (slack/2);
		} else if (balance < 0) {
			pos = slideable.getMinimumPosition();
		} else {
			pos = slideable.getMaximumPosition();
		}
		slideable.setMinimumPosition(pos);
		slideable.setMaximumPosition(pos);
	}

	@Override
	protected void alignRectangular(Rectangular de, Compaction c) {
		alignRectangularAxis(de, c.getHorizontalSegmentSlackOptimisation(), c);
		alignRectangularAxis(de, c.getVerticalSegmentSlackOptimisation(), c);
	}

	private void alignRectangularAxis(Rectangular de, SegmentSlackOptimisation sso, Compaction c) {
		OPair<Slideable<Segment>> oss = sso.getSlideablesFor(de);
		if (oss != null) {
			Slideable<Segment> left = oss.getA();
			Slideable<Segment> right = oss.getB();
			if ((left.getUnderlying().getAlignStyle() == AlignStyle.CENTER) && (right.getUnderlying().getAlignStyle() == AlignStyle.CENTER)) {
				
				long onLeft = getConnectionsCount(de, left.getUnderlying().getAdjoiningSegments(c));
				long onRight = getConnectionsCount(de, right.getUnderlying().getAdjoiningSegments(c));
				
				if (onLeft == onRight) {
					centerSlideables(left, right);
				} else if (onLeft > onRight) {
					leftAlignSlideables(left, right);
				} else {
					rightAlignSlideables(left, right);
				}
			}
		}
	}



	private void leftAlignSlideables(Slideable<Segment> left, Slideable<Segment> right) {
		left.setMaximumPosition(left.getMinimumPosition());
		right.setMaximumPosition(right.getMinimumPosition());
	}



	private void rightAlignSlideables(Slideable<Segment> left, Slideable<Segment> right) {
		left.setMinimumPosition(left.getMaximumPosition());
		right.setMinimumPosition(right.getMaximumPosition());
	}



	private void centerSlideables(Slideable<Segment> left, Slideable<Segment> right) {
		Integer leftMin = left.getMinimumPosition();
		Integer rightMax = right.getMaximumPosition();
		int leftSlack = left.getMaximumPosition() - leftMin;
		int rightSlack = rightMax - leftMin;

		int slackToUse = Math.min(leftSlack, rightSlack);
		slackToUse = slackToUse / 2;
		left.setMinimumPosition(leftMin + slackToUse);
		right.setMaximumPosition(rightMax - slackToUse);
	}



	private long getConnectionsCount(Rectangular de, Set<Segment> adjoiningSegments) {
		if (de instanceof Connected) {
			Connected c = (Connected) de;
			return adjoiningSegments.stream().flatMap(s -> s.getConnections().stream()).filter(cc -> cc.meets(c)).count();
		} else {
			return 0;
		}
	}

}
