package org.kite9.diagram.visualization.compaction.align;

import java.util.List;
import java.util.Set;

import org.kite9.diagram.common.algorithms.so.AlignStyle;
import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.segment.Segment;


/**
 * Figures out how to align connections - usually trying to minimize length.
 * 
 * @author robmoffat
 *
 */
public class ConnectionAligner implements Aligner {
	

	@Override
	public void alignFor(Container co, Set<Rectangular> de, Compaction c, boolean horizontal) {
		List<Segment> segs = horizontal ?  c.getHorizontalSegments() : c.getVerticalSegments();
		alignConnections(segs, de);
		
	}

	private void alignConnections(List<Segment> horizontalSegments, Set<Rectangular> de) {
		horizontalSegments.stream()
			.filter(s -> s.getConnections().size() > 0)
			.filter(s -> hasConnectionMeeting(s, de))
			.forEach(s -> alignSegment(s));
	}
	
	private boolean hasConnectionMeeting(Segment s, Set<Rectangular> de) {
		for (Connection c : s.getConnections()) {
			if (de.contains(c.getFrom()) || (de.contains(c.getTo()))) {
				return true;
			}
		}
		
		return false;
	}

	private void alignSegment(Segment s) {
		Slideable<Segment> sl = s.getSlideable();
		if (s.getAlignStyle() == AlignStyle.MAX) {
			Integer max = sl.getMaximumPosition();
			sl.setMinimumPosition(max);
		} else if (s.getAlignStyle() == AlignStyle.MIN) {
			int min = sl.getMinimumPosition();
			sl.setMaximumPosition(min);
		} else {
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
	}


	@Override
	public boolean willAlign(Rectangular de, boolean horizontal) {
		return true;
	}

}
