package org.kite9.diagram.visualization.compaction.slideable.temp;

import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.style.DiagramElementSizing;
import org.kite9.diagram.visualization.compaction.AbstractCompactionStep;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.Compactor;
import org.kite9.diagram.visualization.compaction.Embedding;
import org.kite9.diagram.visualization.compaction.segment.Segment;
import org.kite9.diagram.visualization.compaction.slideable.SegmentSlackOptimisation;
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
public class MinimizeCompactionStep extends AbstractCompactionStep {
	

	public MinimizeCompactionStep(CompleteDisplayer cd) {
		super(cd);
	}


	@Override
	public void compact(Compaction c, Embedding e, Compactor rc) {
		e.getHorizontalSegments(c).stream()
			.flatMap(s -> s.getUnderlyingInfo().stream())
			.map(ui -> ui.getDiagramElement())
			.filter(de -> de instanceof Rectangular)
			.map(de -> (Rectangular) de)
			.forEach(r -> minimizeRectangular(r, c));
	}
	
	private void minimizeRectangular(Rectangular r, Compaction c) {
		DiagramElementSizing sizing = r.getSizing();
		
		if (sizing == DiagramElementSizing.MINIMIZE) {
			OPair<Slideable<Segment>> hs = c.getHorizontalSegmentSlackOptimisation().getSlideablesFor(r);
			OPair<Slideable<Segment>> vs = c.getVerticalSegmentSlackOptimisation().getSlideablesFor(r);
			if ((hs != null) && (vs != null)) {
				// sometimes, we might not display everything (e.g. labels)
				log.send("Minimizing Distance "+r);
				minimizeDistance(c.getHorizontalSegmentSlackOptimisation(), hs.getA(), hs.getB());
				minimizeDistance(c.getVerticalSegmentSlackOptimisation(), vs.getA(), vs.getB());
			}			
		}
	}

	private void minimizeDistance(SegmentSlackOptimisation opt, Slideable<Segment> from, Slideable<Segment> to) {
		Integer minDist = from.minimumDistanceTo(to);
		opt.ensureMaximumDistance(from, to, minDist);
	}

	public String getPrefix() {
		return "MINC";
	}

	public boolean isLoggingEnabled() {
		return true;
	}


}
