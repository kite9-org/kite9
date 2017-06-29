package org.kite9.diagram.visualization.compaction;

import java.util.Set;
import java.util.stream.Collectors;

import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.elements.DirectionEnforcingElement;
import org.kite9.diagram.common.elements.PositionAction;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.compaction.segment.Segment;
import org.kite9.diagram.visualization.compaction.segment.Side;
import org.kite9.diagram.visualization.compaction.segment.UnderlyingInfo;
import org.kite9.diagram.visualization.compaction.slideable.SegmentSlackOptimisation;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;


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

	protected double getMinimumDistance(Segment first, Segment second, Segment along, boolean concave) {
		boolean horizontalDartFirst = first.getDimension() == PositionAction.XAction;
		boolean horizontalDartSecond = second.getDimension() == PositionAction.XAction;
		
		if (horizontalDartFirst != horizontalDartSecond) {
			throw new Kite9ProcessingException();
		}
		
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
	
	private DiagramElement moveUp(DiagramElement move, int toDepth, int cDepth) {
		while (cDepth > toDepth) {
			move = move.getParent();
			cDepth--;
		}
		
		return move;
	}
 
	private boolean contains(DiagramElement a, DiagramElement b) {
		int ad = a.getDepth();
		int bd = b.getDepth();
		
		if ((ad < bd) && (a instanceof Container)) {
			// b might be in a
			b = moveUp(b, ad+1, bd);
			return ((Container)a).getContents().contains(b);
		} else if ((ad > bd) && (b instanceof Container)) {
			// a might be in b
			a = moveUp(a, bd+1, ad);
			return ((Container)b).getContents().contains(a);
		} else {
			return false;
		}
	}

	private boolean needsLength(DiagramElement a, DiagramElement b) {
		if ((a instanceof DirectionEnforcingElement) || (b instanceof DirectionEnforcingElement)) {
			return false;
		}
		
		return true;
	}
	
	
	
	
	
	/**
	 * Uses the SlackOptimisation to set a minimum distance between outside and inside parts.
	 */
	protected void separate(Slideable<Segment> s1, Slideable<Segment> s2) {
		double minDistance = getMinimumDistance(s1.getUnderlying(), s2.getUnderlying(), null, true);
		s1.getSlackOptimisation().ensureMinimumDistance(s1, s2, (int) minDistance);
	}
//
//
//	private void separate(Vertex a, Segment to, Segment extend, Direction d, List<Dart> result, Compaction c) {
//		if (to.getVerticesInSegment().contains(a)) {
//			return;
//		}
//		
//		Vertex rv = c.createCompactionVertex(to, extend);
//		double length = getMinimumDistance(c, a, rv, d);
//		Dart da = c.getOrthogonalization().createDart(a, rv, null, d, length);
//		da.setChangeCost(Dart.EXTEND_IF_NEEDED, null);
//		result.add(da);
//	}

	protected void optionallyCenter(Compaction c, OPair<Slideable<Segment>> perp, OPair<Slideable<Segment>> along) {
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

	private Set<Connection> getLeavingConnections(Segment s, Compaction c) {
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
	
}
