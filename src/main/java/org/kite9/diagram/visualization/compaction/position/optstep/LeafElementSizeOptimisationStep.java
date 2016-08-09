package org.kite9.diagram.visualization.compaction.position.optstep;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.kite9.diagram.adl.Contained;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.adl.Leaf;
import org.kite9.diagram.common.algorithms.so.AlignStyle;
import org.kite9.diagram.common.algorithms.so.OptimisationStep;
import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.elements.Vertex;
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

	/**
	 * Works out which slideables are incident on a glyph, container or arrow.
	 */
	private Map<DiagramElement, List<Slideable>> computeElementParts(SegmentSlackOptimisation opt) {
		Map<DiagramElement, List<Slideable>> map = new LinkedHashMap<DiagramElement, List<Slideable>>();

		for (Slideable s : opt.getCanonicalOrder()) {
			Segment seg = (Segment) s.getUnderlying();

			for (Vertex v : seg.getVerticesInSegment()) {
				DiagramElement underlying = v.getOriginalUnderlying();

					if (underlying instanceof Leaf) {
						List<Slideable> parts = map.get(underlying);
						if (parts == null) {
							parts = new ArrayList<Slideable>();
							map.put(underlying, parts);
						}

						// add the current segment variable
						if (!parts.contains(s)) {
							parts.add(s);
						}
					}
				
			}
		}

		return map;
	}

	public void optimise(Compaction c, SegmentSlackOptimisation x, SegmentSlackOptimisation y) {
		optimise(y);
		optimise(x);
	}
	
	static class ElementSlidables implements Comparable<ElementSlidables> {
		
		Slideable ls;
		Slideable rs;
		
		int priority;

		public int compareTo(ElementSlidables o) {
			return ((Integer)this.priority).compareTo(o.priority);
		}
		
	}

	/**
	 * This labels pairs of attr in the diagram with the alignment they should have,
	 * and assigns a unique priority to the attr in the diagram, so that those with lowest
	 * priority number receive preference on size.
	 */
	public void minimizeDiagramElementSizes(SegmentSlackOptimisation opt, Map<DiagramElement, List<Slideable>> glyphParts) {
		List<ElementSlidables> toDo = new ArrayList<ElementSlidables>(glyphParts.size());
		
		for (Entry<DiagramElement, List<Slideable>> entry : glyphParts.entrySet()) {
			if (entry.getKey() instanceof Contained) {
				List<Slideable> val = entry.getValue();
				ElementSlidables es = new ElementSlidables();
				es.ls = val.get(0);
				es.rs = val.get(val.size()-1);
				es.priority = val.size();
				toDo.add(es);
			}
		}
		
		Collections.sort(toDo);
		for (ElementSlidables es : toDo) {
			log.send(log.go() ? null : es.ls+" "+es.ls.minRight);
		}
		
		for (int i = 0; i < toDo.size(); i++) {
			ElementSlidables es = toDo.get(i);
			
			es.ls.setAlignTo(es.rs);
			es.rs.setAlignTo(es.ls);
			es.ls.setAlignStyle(AlignStyle.RIGHT);
			
			Slideable from = es.ls;
			Slideable to = es.rs;
			
			Integer minDist = from.minimumDistanceTo(to);
			log.send(log.go() ? null : "Setting max dist of " +minDist+ " from "+from+" to "+to);	
			
			int currentDist = to.getMinimumPosition() - from.getMinimumPosition();
			
			int push = currentDist - minDist;
			from.increaseMinimum(from.getMinimumPosition() + push);
			
			opt.ensureMaximumDistance(from, to, minDist, true);
		}
	}
	

	public void optimise(SegmentSlackOptimisation opt) {
		Map<DiagramElement, List<Slideable>> parts = computeElementParts(opt);
		minimizeDiagramElementSizes(opt, parts);
	}

	public String getPrefix() {
		return "CEAO";
	}

	public boolean isLoggingEnabled() {
		return true;
	}

}
