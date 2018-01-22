package org.kite9.diagram.visualization.compaction.slideable;

import java.util.Set;

import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.style.DiagramElementSizing;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.segment.Segment;
import org.kite9.diagram.visualization.display.CompleteDisplayer;

/**
 * In some sorted order, minimizes the size of Rectangular elements in the
 * diagram.
 * 
 * @see ContainedElementSizeOptimisationStep
 * @see LinkLengthReductionOptimisationStep
 * 
 * @author robmoffat
 * 
 */
public class MinimizeCompactionStep extends AbstractSizingCompactionStep {

	public MinimizeCompactionStep(CompleteDisplayer cd) {
		super(cd);
	}

	@Override
	public boolean filter(Rectangular r) {
		return (r instanceof Container) && (((Container) r).getSizing() == DiagramElementSizing.MINIMIZE) ;
	}

	public void performSizing(Rectangular r, Compaction c, boolean horizontal) {
		SegmentSlackOptimisation hsso = c.getHorizontalSegmentSlackOptimisation();
		OPair<Slideable<Segment>> hs = hsso.getSlideablesFor(r);
		SegmentSlackOptimisation vsso = c.getVerticalSegmentSlackOptimisation();
		OPair<Slideable<Segment>> vs = vsso.getSlideablesFor(r);
		if ((hs != null) && (vs != null)) {
			log.send("Minimizing Distance " + r);
			if (horizontal) {
				minimizeDistance(vsso, vs.getA(), vs.getB());
			} else {
				minimizeDistance(hsso, hs.getA(), hs.getB());
			}
		}
	}

	private int minimizeDistance(SegmentSlackOptimisation opt, Slideable<Segment> from, Slideable<Segment> to) {
		Integer minDist = from.minimumDistanceTo(to);
		opt.ensureMaximumDistance(from, to, minDist);
		
		return minDist;
	}

	public String getPrefix() {
		return "MINC";
	}

	public boolean isLoggingEnabled() {
		return true;
	}

	/**
	 * Returns in an order to maximize number of centerings.
	 */
	public int compare(Rectangular a, Rectangular b, Compaction c, boolean horizontal) {
		if (a.getDepth() != b.getDepth()) {
			return -((Integer) a.getDepth()).compareTo(b.getDepth());
		}

		if ((!(a instanceof Connected)) && (!(b instanceof Connected))) {
			return 0;
		} else if (!(a instanceof Connected)) {
			return -1;
		} else if (!(b instanceof Connected)) {
			return 1;
		} else {
			// return elements with least number of connections on a side
			int ac = maxLeavings(a, c, horizontal);
			int bc = maxLeavings(b, c, horizontal);
			if (bc != ac) {
				return ((Integer) ac).compareTo(bc);
			}
		}

		return b.getID().compareTo(a.getID());
	}

	private int maxLeavings(Rectangular a, Compaction c, boolean horizontal) {
		if (horizontal) {
			return maxLeavingsInAxis(a, c.getHorizontalSegmentSlackOptimisation(), c);
		} else {
			return maxLeavingsInAxis(a, c.getVerticalSegmentSlackOptimisation(), c);
		}
	}

	private int maxLeavingsInAxis(Rectangular a, SegmentSlackOptimisation sso, Compaction c) {
		OPair<Slideable<Segment>> along = sso.getSlideablesFor(a);
		return Math.max(leavingsOnSide(along.getA(), c), leavingsOnSide(along.getB(), c));
	}

	private int leavingsOnSide(Slideable<Segment> a2, Compaction c) {
		Set<Connection> connections = getLeavingConnections(a2.getUnderlying(), c);
		return connections.size();
	}

}
