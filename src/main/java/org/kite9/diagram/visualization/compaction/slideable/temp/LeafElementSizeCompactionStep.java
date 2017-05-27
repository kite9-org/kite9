package org.kite9.diagram.visualization.compaction.slideable.temp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.kite9.diagram.common.algorithms.so.AlignStyle;
import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.style.DiagramElementSizing;
import org.kite9.diagram.visualization.compaction.AbstractCompactionStep;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.Compactor;
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
public class LeafElementSizeCompactionStep extends AbstractCompactionStep {
	

	public LeafElementSizeCompactionStep(CompleteDisplayer cd) {
		super(cd);
	}

	@Override
	public void compact(Compaction c, Rectangular r, Compactor rc) {
		DiagramElementSizing sizing = r.getSizing();
		log.send("Aligning: "+r+" "+sizing);
		SegmentSlackOptimisation soH = c.getHorizontalSegmentSlackOptimisation();
		SegmentSlackOptimisation soV = c.getVerticalSegmentSlackOptimisation();

		size1d(r, sizing, soH);
		size1d(r, sizing, soV);	
	}

	private void size1d(Rectangular r, DiagramElementSizing sizing, SegmentSlackOptimisation so) {
		OPair<Slideable<Segment>> slideables = so.getSlideablesFor(r);
		alignPair(so, slideables.getA(), slideables.getB(), sizing, r);
	}

	

	private void maximizeDistance(SegmentSlackOptimisation xo, Slideable<Segment> from, Slideable<Segment> to) {
		int slackAvailable = to.getMaximumPosition() - from.getMinimumPosition();
		from.setMaximumPosition(from.getMinimumPosition());
		xo.ensureMinimumDistance(from, to, slackAvailable);
	}
	

	private void minimizeDistance(SegmentSlackOptimisation opt, Slideable<Segment> from, Slideable<Segment> to) {
		Integer minDist = from.minimumDistanceTo(to);
		
		int slackAvailable = to.getMaximumPosition() - from.getMinimumPosition();
		opt.ensureMaximumDistance(from, to, minDist);
	}

	private void alignPair(SegmentSlackOptimisation opt, Slideable<Segment> from, Slideable<Segment> to, DiagramElementSizing sizing, DiagramElement underlying) {
		log.send("Aligning: "+de+" "+process+" "+from+" "+to);
		from.setAlignTo(to);
		to.setAlignTo(from);
		to.setAlignStyle(AlignStyle.RIGHT);
		
		if (sizing == DiagramElementSizing.MAXIMIZE) {
			maximizeDistance(opt, from, to);
		} else if (sizing == DiagramElementSizing.MINIMIZE) {
			minimizeDistance(opt, from, to);
		}
		
	}

	public String getPrefix() {
		return "LESO";
	}

	public boolean isLoggingEnabled() {
		return true;
	}


}
