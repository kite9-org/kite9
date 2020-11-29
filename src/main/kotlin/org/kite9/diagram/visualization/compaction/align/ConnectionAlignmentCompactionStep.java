package org.kite9.diagram.visualization.compaction.align;

import java.util.List;

import org.kite9.diagram.common.algorithms.so.AlignStyle;
import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.CompactionStep;
import org.kite9.diagram.visualization.compaction.Compactor;
import org.kite9.diagram.visualization.compaction.Embedding;
import org.kite9.diagram.visualization.compaction.segment.Segment;


/**
 * Figures out how to align connections - usually trying to minimize length.
 * 
 * @author robmoffat
 *
 */
public class ConnectionAlignmentCompactionStep implements CompactionStep {
	
	@Override
	public void compact(Compaction c, Embedding e, Compactor rc) {
		if  (e.isTopEmbedding()) {
			alignConnections(c.getHorizontalSegments());
			alignConnections(c.getVerticalSegments());
		}
	}

	private void alignConnections(List<Segment> horizontalSegments) {
		horizontalSegments.stream()
			.filter(s -> s.getConnections().size() > 0)
			.forEach(s -> alignSegment(s));
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

}
