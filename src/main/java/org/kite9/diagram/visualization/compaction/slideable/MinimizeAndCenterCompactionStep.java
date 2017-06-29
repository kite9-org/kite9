package org.kite9.diagram.visualization.compaction.slideable;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.style.DiagramElementSizing;
import org.kite9.diagram.visualization.compaction.AbstractCompactionStep;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.Compactor;
import org.kite9.diagram.visualization.compaction.Embedding;
import org.kite9.diagram.visualization.compaction.segment.Segment;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.framework.common.Kite9ProcessingException;

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
		e.getHorizontalSegments(c).stream()
			.flatMap(s -> s.getUnderlyingInfo().stream())
			.map(ui -> ui.getDiagramElement())
			.filter(de -> de instanceof Rectangular)
			.map(de -> (Rectangular) de)
			.distinct()
			.forEach(r -> minimizeRectangular(r, c));
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
				log.send("Minimizing Distance "+r);

				
				if (r instanceof Connected) {
					centerSingleConnections(c, hsso, hs, vsso, vs);
				}	

				
				minimizeDistance(hsso, hs.getA(), hs.getB());
				minimizeDistance(vsso, vs.getA(), vs.getB());
			}			
		}
	}


	private void centerSingleConnections(Compaction c, SegmentSlackOptimisation hsso, OPair<Slideable<Segment>> hs, SegmentSlackOptimisation vsso, OPair<Slideable<Segment>> vs) {
		optionallyCenter(c, hs, vsso, vs);
		optionallyCenter(c, vs, hsso, hs);
	}


	private void optionallyCenter(Compaction c, OPair<Slideable<Segment>> perp, SegmentSlackOptimisation alongSSO, OPair<Slideable<Segment>> along) {
		Slideable<Segment> from = along.getA();
		Slideable<Segment> to = along.getB();
		
		Set<Connection> leavingConnectionsA = getLeavingConnections(perp.getA().getUnderlying(), c);
		Set<Connection> leavingConnectionsB = getLeavingConnections(perp.getB().getUnderlying(), c);
		
		int halfDist = 0;
		
		Slideable<Segment> connectionSegmentA = null;
		Slideable<Segment> connectionSegmentB = null;
		
		if (leavingConnectionsA.size() == 1) {
			connectionSegmentA = getConnectionSegment(perp.getA(), c);
			
			halfDist = Math.max(halfDist, from.minimumDistanceTo(connectionSegmentA));
			halfDist = Math.max(halfDist, connectionSegmentA.minimumDistanceTo(to));
		}
		
		if (leavingConnectionsB.size() == 1) {
			connectionSegmentB = getConnectionSegment(perp.getB(), c);
			
			halfDist = Math.max(halfDist, from.minimumDistanceTo(connectionSegmentB));
			halfDist = Math.max(halfDist, connectionSegmentB.minimumDistanceTo(to));
		}
		
		int totalDist = from.minimumDistanceTo(to);
			
		if (totalDist > halfDist * 2) {
			halfDist = (int) Math.ceil(totalDist / 2d);
		} 		
			
		if (connectionSegmentA != null) {
			alongSSO.ensureMinimumDistance(from, connectionSegmentA, halfDist);
			alongSSO.ensureMinimumDistance(connectionSegmentA, to, halfDist);
		}
		
		if (connectionSegmentB != null) {
			alongSSO.ensureMinimumDistance(from, connectionSegmentB, halfDist);
			alongSSO.ensureMinimumDistance(connectionSegmentB, to, halfDist);
		}
		
	}
	
	public Set<Connection> getLeavingConnections(Segment s, Compaction c) {
		Set<Connection> leavingConnections = s.getAdjoiningSegments(c).stream()
			.flatMap(seg -> seg.getConnections().stream())
			.collect(Collectors.toSet());
			
		return leavingConnections;
	}

	private Slideable<Segment> getConnectionSegment(Slideable<Segment> s1, Compaction c) {
		return s1.getUnderlying().getAdjoiningSegments(c).stream()
			.filter(s -> s.getConnections().size() > 0)
			.map(s -> s.getSlideable()).findFirst().orElseThrow(() -> new Kite9ProcessingException());
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
