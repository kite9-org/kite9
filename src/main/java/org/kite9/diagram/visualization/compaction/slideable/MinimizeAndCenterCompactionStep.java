package org.kite9.diagram.visualization.compaction.slideable;

import java.util.Set;

import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.Diagram;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.style.DiagramElementSizing;
import org.kite9.diagram.visualization.compaction.AbstractCompactionStep;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.Compactor;
import org.kite9.diagram.visualization.compaction.Embedding;
import org.kite9.diagram.visualization.compaction.segment.Segment;
import org.kite9.diagram.visualization.display.CompleteDisplayer;

/**
 * This optimisation annotates the {@link Slideable}'s in such a way as to
 * describe which slideables should be a minimal distance apart. The basic
 * outcome is that you would prefer all pairs of slideables that start and end a
 * glyph or arrow to be the minimum distance apart. 
 * 
 * Unfortunately, this can cause competition between different pairs of glyphs,
 * so we prioritise glyph size based on number of connections on a side.
 * 
 * To start with, segments are positioned from left-to-right, with minimum and maximum
 * positions calculated.  Rendering the diagram using all of the maximum positions would mean
 * everything forced to the right or bottom (like gravity).  Using minimum positions would mean everything
 * aligned to the top or left.  
 * 
 * Having done this, there is usually space in between the max and min positions for optimisation.
 * There are clearly cases where moving
 * intermediate segments higher would result in a better layout (i.e. to reduce
 * the sizes of glyphs). 
 * 
 * Some segment pairs are given a right alignment then: they would like to use this
 * slack to move right to get as close to their alignment partner as possible.  Having right-aligned
 * something, the maximum width is set on the segment pair in order that the later optimisations
 * don't ruin this minimisation.
 * 
 * @see ContainedElementSizeOptimisationStep
 * @see LinkLengthReductionOptimisationStep
 * 
 * @author robmoffat
 * 
 */
public class MinimizeAndCenterCompactionStep extends AbstractCompactionStep {
	

	public MinimizeAndCenterCompactionStep(CompleteDisplayer cd) {
		super(cd);
	}


	@Override
	public void compact(Compaction c, Embedding e, Compactor rc) {
		if (e.isTopEmbedding()) {
			e.getHorizontalSegments(c).stream()
				.flatMap(s -> s.getUnderlyingInfo().stream())
				.map(ui -> ui.getDiagramElement())
				.filter(de -> de instanceof Rectangular)
				.map(de -> (Rectangular) de)
				.distinct()
				.sorted((a, b) -> compare(a,b,c))
				.forEach(r -> minimizeRectangular(r, c));
		}
	}

	/**
	 * Returns in an order to maximize number of centerings. 
	 */
	private int compare(Rectangular a, Rectangular b, Compaction c) {
		if (a.getDepth() != b.getDepth()) {
			return ((Integer) a.getDepth()).compareTo(b.getDepth());
		} 
		
		if (!(a instanceof Connected)) {
			return -1;  
		} else if (!(b instanceof Connected)) {
			return 1;
		} else {
			// return elements with least number of connections on a side
			int ac = maxLeavings(a, c);
			int bc = maxLeavings(b, c);
			if (bc != ac) {
				return ((Integer) ac)
						.compareTo(bc);
			}
		} 
		
		return b.getID().compareTo(a.getID());
	}


	private int maxLeavings(Rectangular a, Compaction c) {
		return Math.max(maxLeavingsInAxis(a, c.getHorizontalSegmentSlackOptimisation(), c), 
				maxLeavingsInAxis(a, c.getVerticalSegmentSlackOptimisation(), c));
	}


	private int maxLeavingsInAxis(Rectangular a, SegmentSlackOptimisation sso, Compaction c) {
		OPair<Slideable<Segment>> along = sso.getSlideablesFor(a);
		return Math.max(leavingsOnSide(along.getA(),c),
				leavingsOnSide(along.getB(), c));
	}


	private int leavingsOnSide(Slideable<Segment> a2, Compaction c) {
		Set<Connection> connections = getLeavingConnections(a2.getUnderlying(), c);
		return connections.size();
	}


	private void minimizeRectangular(Rectangular r, Compaction c) {
		DiagramElementSizing sizing = r.getSizing();
		
		if (sizing == DiagramElementSizing.MINIMIZE) {
			SegmentSlackOptimisation hsso = c.getHorizontalSegmentSlackOptimisation();
			OPair<Slideable<Segment>> hs = hsso.getSlideablesFor(r);
			SegmentSlackOptimisation vsso = c.getVerticalSegmentSlackOptimisation();
			OPair<Slideable<Segment>> vs = vsso.getSlideablesFor(r);
			if ((hs != null) && (vs != null)) {
				// sometimes, we might not display everything (e.g. labels)
				log.send("Centering Connections "+r);

				
				if (r instanceof Connected) {
					centerSingleConnections(c, hs, vs);
				}	

				log.send("Minimizing Distance "+r);				
				minimizeDistance(hsso, hs.getA(), hs.getB());
				minimizeDistance(vsso, vs.getA(), vs.getB());
			}			
		}
	}


	private void centerSingleConnections(Compaction c, OPair<Slideable<Segment>> hs, OPair<Slideable<Segment>> vs) {
		alignSingleConnections(c, hs, vs);
		alignSingleConnections(c, vs, hs);
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


}
