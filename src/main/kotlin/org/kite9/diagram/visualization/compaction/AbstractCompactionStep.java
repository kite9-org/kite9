package org.kite9.diagram.visualization.compaction;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.elements.Dimension;
import org.kite9.diagram.common.elements.DirectionEnforcingElement;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.SizedRectangular;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.style.DiagramElementSizing;
import org.kite9.diagram.visualization.compaction.segment.Segment;
import org.kite9.diagram.visualization.compaction.segment.Side;
import org.kite9.diagram.visualization.compaction.segment.UnderlyingInfo;
import org.kite9.diagram.visualization.compaction.slideable.SegmentSlackOptimisation;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.logging.Kite9Log;
import org.kite9.diagram.logging.Logable;
import org.kite9.diagram.logging.LogicException;


/**
 * This contains utility methods to deal with insertion of sub-graphs within the overall graph.
 * You should extend this wherever you need to add vertices to a segment.  
 */
public abstract class AbstractCompactionStep implements CompactionStep, Logable {

	protected Kite9Log log = new Kite9Log(this);
	
	protected CompleteDisplayer displayer;

	public AbstractCompactionStep(CompleteDisplayer cd) {
		this.displayer = cd;
	}
	
	@Override
	public String getPrefix() {
		return null;
	}

	@Override
	public boolean isLoggingEnabled() {
		return true;
	}

	public double getMinimumDistance(Slideable<Segment> froms, Slideable<Segment> tos, Direction d) {
		return froms.minimumDistanceTo(tos);
	}

	public double getMinimumDistance(Segment first, Segment second, Segment along, boolean concave) {
		boolean horizontalDartFirst = first.getDimension() == Dimension.V;
		boolean horizontalDartSecond = second.getDimension() == Dimension.V;
		
		if (horizontalDartFirst != horizontalDartSecond) {
			throw new LogicException();
		}
		
		
		if ((first.getUnderlyingInfo().size() > 1) && (second.getUnderlyingInfo().size() > 1)) {
			// we're in a grid, look for common diagram elements
			Set<Rectangular> combined = new HashSet<>(first.getRectangulars());
			Set<Rectangular> secondRs = second.getRectangulars();
			combined.retainAll(secondRs);
		
			if (combined.size() == 1) {
				// ok, run just the single found combination
				double max = 0;
				for (UnderlyingInfo fromUI : first.getUnderlyingInfo()) {
					if (combined.contains(fromUI.getDiagramElement())) {
						return getMinimumDistance(horizontalDartFirst, fromUI, second, along, concave);
					}
				}
				
				throw new LogicException();
			}
		} 
		
		// ok, run all the combinations
		double max = 0;
		for (UnderlyingInfo fromUI : first.getUnderlyingInfo()) {
			max = Math.max(max, getMinimumDistance(horizontalDartFirst, fromUI, second, along, concave));
		}
		
		return max;
		
	}
	
	private double getMinimumDistance(boolean horizontalDart, UnderlyingInfo fromUI, Segment second, Segment along, boolean concave) {
		double max = 0;
		for (UnderlyingInfo toUI : second.getUnderlyingInfo()) {
			max = Math.max(max, getMinimumDistance(horizontalDart, fromUI, toUI, along, concave));
		}
		
		return max;
	}

	private double getMinimumDistance(boolean horizontalDart, UnderlyingInfo fromUI, UnderlyingInfo toUI, Segment along, boolean concave) {
		DiagramElement fromde = fromUI.getDiagramElement();
		Direction fromUnderlyingSide = convertSideToDirection(horizontalDart, fromUI.getSide(), true);
		DiagramElement tode = toUI.getDiagramElement();
		Direction toUnderlyingSide = convertSideToDirection(horizontalDart, toUI.getSide(), false);

		if (!needsLength(fromde, tode)) {
			return 0;
		}
		
		DiagramElement alongDe = getAlongDiagramElement(along);
		
		
		return displayer.getMinimumDistanceBetween(fromde, fromUnderlyingSide, tode, toUnderlyingSide, horizontalDart ? Direction.RIGHT : Direction.DOWN, alongDe, concave);
	}

	private DiagramElement getAlongDiagramElement(Segment along) {
		if (along == null) {
			return null;
		}
		DiagramElement best = along.getUnderlyingWithSide(Side.NEITHER);
		if (best == null) {
			return along.getUnderlyingInfo().stream().map(ui -> ui.getDiagramElement()).findFirst().orElse(null);
		}
		return best;
	}
	
	private Direction convertSideToDirection(boolean horizontalDart, Side side, boolean first) {
		switch (side) {
		case END:
			return horizontalDart ? Direction.RIGHT : Direction.DOWN;
		case START:
			return horizontalDart ? Direction.LEFT : Direction.UP;
		default:
			if (horizontalDart) {
				return first ? Direction.RIGHT : Direction.LEFT;
			} else {
				return first ? Direction.DOWN : Direction.UP;
			}
		}
	}
	
	
	private boolean needsLength(DiagramElement a, DiagramElement b) {
		if ((a instanceof DirectionEnforcingElement) || (b instanceof DirectionEnforcingElement)) {
			return false;
		}
		
		return true;
	}
	
	protected void separate(Slideable<Segment> s1, FaceSide fs) {
		for (Slideable<Segment> s2 : fs.getAll()) {
			separate(s1, s2);
		}
	}
	
	protected void separate(FaceSide fs, Slideable<Segment> s2) {
		for (Slideable<Segment> s1 : fs.getAll()) {
			separate(s1, s2);
		}
	}

	protected void separate(Slideable<Segment> s1, Slideable<Segment> s2) {
		double minDistance = getMinimumDistance(s1.getUnderlying(), s2.getUnderlying(), null, true);
		s1.getSlackOptimisation().ensureMinimumDistance(s1, s2, (int) minDistance);
	}

	
	protected AlignmentResult alignSingleConnections(Compaction c, Connected r, boolean horizontal, boolean withCheck) {
		SegmentSlackOptimisation hsso = c.getHorizontalSegmentSlackOptimisation();
		OPair<Slideable<Segment>> hs = hsso.getSlideablesFor(r);
		SegmentSlackOptimisation vsso = c.getVerticalSegmentSlackOptimisation();
		OPair<Slideable<Segment>> vs = vsso.getSlideablesFor(r);
		
		boolean minimizing = (r instanceof SizedRectangular) ? ((SizedRectangular)r).getSizing(horizontal) == DiagramElementSizing.MINIMIZE : true;
		
		if (horizontal) {
			return alignSingleConnections(c, hs, vs, withCheck, minimizing);
		} else {
			return alignSingleConnections(c, vs, hs, withCheck, minimizing);
		}
	}
	
	public static class AlignmentResult {
		
		public AlignmentResult(int midPoint, boolean safe) {
			super();
			this.midPoint = midPoint;
			this.safe = safe;
		}

		public final int midPoint;
		public final boolean safe;
		
	}

	/**
	 * Returns the half-dist value if an alignment was made, otherwise null.
	 */
	protected AlignmentResult alignSingleConnections(Compaction c, OPair<Slideable<Segment>> perp, OPair<Slideable<Segment>> along, boolean checkNeeded, boolean minimizingContainer) {
		SegmentSlackOptimisation alongSSO = (SegmentSlackOptimisation) along.getA().getSlackOptimisation();
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
		
		if (leavingConnectionsA.size() + leavingConnectionsB.size() == 0) {
			return null; 
		}
			
		int totalDist = from.minimumDistanceTo(to);
		if (totalDist > halfDist * 2) {
			double halfTotal = ((double) totalDist) / 2d;
			halfDist = (int) Math.floor(halfTotal);
		}
			
		if (halfDist > 0) {
			if (connectionSegmentA != null) {
				addWithCheck(alongSSO, from, halfDist, connectionSegmentA, checkNeeded);
				addWithCheck(alongSSO, connectionSegmentA, halfDist, to, checkNeeded);
			}
			
			if (connectionSegmentB != null) {
				addWithCheck(alongSSO, from, halfDist, connectionSegmentB, checkNeeded);
				addWithCheck(alongSSO, connectionSegmentB, halfDist, to, checkNeeded);
			}
			
			if ((connectionSegmentA != null) || (connectionSegmentB != null)) {
				boolean safe = (leavingConnectionsA.size() < 2) && (leavingConnectionsB.size() < 2) && (minimizingContainer);
				return new AlignmentResult(halfDist, safe);
			}
			
		}
		
		return null;
	
	}

	private void addWithCheck(SegmentSlackOptimisation alongSSO, Slideable<Segment> from, int dist, Slideable<Segment> to, boolean checkNeeded) {
		if (checkNeeded) {
			if (!from.canAddMinimumForwardConstraint(to, dist)) {
				return;
			}
		}
		
		alongSSO.ensureMinimumDistance(from, to, dist);
	}

	protected Set<Connection> getLeavingConnections(Segment s, Compaction c) {
		Set<Connection> leavingConnections = s.getAdjoiningSegments(c).stream()
			.flatMap(seg -> seg.getConnections().stream())
			.collect(Collectors.toSet());
			
		return leavingConnections;
	}

	private Slideable<Segment> getConnectionSegment(Slideable<Segment> s1, Compaction c) {
		return s1.getUnderlying().getAdjoiningSegments(c).stream()
			.filter(s -> s.getConnections().size() > 0)
			.map(s -> s.getSlideable()).findFirst().orElseThrow(() -> new LogicException());
	}
	
}
