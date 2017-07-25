package org.kite9.diagram.visualization.compaction.slideable;

import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.style.DiagramElementSizing;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.segment.Segment;
import org.kite9.diagram.visualization.display.CompleteDisplayer;

public class MaximizeCompactionStep extends AbstractSizingCompactionStep {

	public MaximizeCompactionStep(CompleteDisplayer cd) {
		super(cd);
	}

	@Override
	public boolean filter(Rectangular r) {
		return (r.getSizing() == DiagramElementSizing.MAXIMIZE);
	}

	/**
	 * Orders top-down
	 */
	public int compare(Rectangular a, Rectangular b, Compaction c) {
		return - ((Integer) a.getDepth()).compareTo(b.getDepth());
	}

	public void performSizing(Rectangular r, Compaction c) {
		SegmentSlackOptimisation hsso = c.getHorizontalSegmentSlackOptimisation();
		OPair<Slideable<Segment>> hs = hsso.getSlideablesFor(r);
		SegmentSlackOptimisation vsso = c.getVerticalSegmentSlackOptimisation();
		OPair<Slideable<Segment>> vs = vsso.getSlideablesFor(r);
		if ((hs != null) && (vs != null)) {
			log.send("Maximizing Distance " + r);
			maximizeDistance(hsso, hs.getA(), hs.getB());
			maximizeDistance(vsso, vs.getA(), vs.getB());
		}
	}

	private void maximizeDistance(SegmentSlackOptimisation sso, Slideable<Segment> min, Slideable<Segment> max) {
		max.setMinimumPosition(max.getMaximumPosition());
		min.setMaximumPosition(min.getMinimumPosition());
//		int maxDistance = max.getMaximumPosition() - min.getMinimumPosition();
//		sso.ensureMinimumDistance(min, max, maxDistance);
	}

}
