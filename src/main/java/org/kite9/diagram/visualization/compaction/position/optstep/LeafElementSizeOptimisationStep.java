package org.kite9.diagram.visualization.compaction.position.optstep;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.kite9.diagram.common.algorithms.so.AlignStyle;
import org.kite9.diagram.common.algorithms.so.OptimisationStep;
import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.Segment;
import org.kite9.diagram.visualization.compaction.position.SegmentSlackOptimisation;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;

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
public class LeafElementSizeOptimisationStep implements OptimisationStep, Logable {

	Kite9Log log = new Kite9Log(this);

	

	public void optimise(Compaction c, SegmentSlackOptimisation x, SegmentSlackOptimisation y) {
		minimizeDiagramElementSizes(x);
		minimizeDiagramElementSizes(y);
	}
	
	
	/**
	 * This labels pairs of attr in the diagram with the alignment they should have,
	 * and assigns a unique priority to the attr in the diagram, so that those with lowest
	 * priority number receive preference on size.
	 */
	public void minimizeDiagramElementSizes(SegmentSlackOptimisation opt) {
		opt.updatePositionalOrdering();
		List<OPair<Slideable>> toDo = new ArrayList<>(opt.getRectangularSlideablePairs());
		
		Collections.sort(toDo, new Comparator<OPair<Slideable>>() {

			@Override
			public int compare(OPair<Slideable> o1, OPair<Slideable> o2) {
				int distO1 = getDist1(o1);
				int distO2 = getDist1(o2);
				return ((Integer)distO1).compareTo(distO2);
			}

			private int getDist1(OPair<Slideable> o1) {
				Slideable a = o1.getA();
				Slideable b = o1.getB();
				if ((a == null) || (b == null)) {
					return Integer.MAX_VALUE;
				}
				
				return Math.abs(a.getPositionalOrder() - b.getPositionalOrder());
			}
			
		});
		
		for (int i = 0; i < toDo.size(); i++) {
			OPair<Slideable> es = toDo.get(i);
			Slideable from = es.getA();
			Slideable to = es.getB();
			
			if  ((from != null) && (to != null)) {
				alignPair(opt, from, to);
			} 
		}
	}


	private void alignPair(SegmentSlackOptimisation opt, Slideable from, Slideable to) {
		from.setAlignTo(to);
		to.setAlignTo(from);
		to.setAlignStyle(AlignStyle.RIGHT);
		
		log.send("Adjusting "+((Segment)from.getUnderlying()).getUnderlying());
		Integer minDist = from.minimumDistanceTo(to) + 1  ;
		log.send(log.go() ? null : "Minimum Possible Distance " +minDist+ " from "+from+" to "+to);	
		opt.ensureMaximumDistance(from, to, minDist);
	}

	public String getPrefix() {
		return "LESO";
	}

	public boolean isLoggingEnabled() {
		return true;
	}

}
