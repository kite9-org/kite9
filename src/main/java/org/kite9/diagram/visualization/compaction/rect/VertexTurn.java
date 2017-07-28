package org.kite9.diagram.visualization.compaction.rect;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.elements.vertex.FanVertex;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.style.DiagramElementSizing;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.segment.Segment;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.logging.LogicException;

/**
 * Stores the segments on the stack 
 */
class VertexTurn {
	
	static enum TurnPriority { MINIMIZE_RECTANGULAR(3), CONNECTION(1), MAXIMIZE_RECTANGULAR(0);
		
		private final int factor;
		
		public int getCostFactor() {
			return factor;
		}

		private TurnPriority(int factor) {
			this.factor = factor;
		}
	
	}
	
	public VertexTurn(int number, Compaction c, Slideable<Segment> s, Direction d, Slideable<Segment> startsWith, Slideable<Segment> endsWith) {
		this.d = d;
		this.s = s;
		this.startsWith = startsWith;
		this.endsWith = endsWith;
		this.start = commonVertex(s.getUnderlying(), startsWith.getUnderlying());
		this.end = commonVertex(s.getUnderlying(), endsWith.getUnderlying());
		this.number = number;
		this.c = c;
		this.turnPriority = calculateTurnPriority();
		this.otherSide = this.turnPriority == TurnPriority.MINIMIZE_RECTANGULAR;
	}
	
	public int getNumber() {
		return number;
	}

	private Vertex commonVertex(Segment a, Segment b) {
		Optional<Vertex> out = a.getVerticesInSegment().stream().filter(v -> b.getVerticesInSegment().contains(v)).findFirst();
		if (out.isPresent()) {
			return out.get();
		} 
		
		return null;
	}
	
	public boolean containsVertex(Vertex v) {
		return s.getUnderlying().getVerticesInSegment().contains(v);
	}

	private final Slideable<Segment> s;
	private final int number;
	private Slideable<Segment> startsWith;
	private Vertex start;
	private Slideable<Segment> endsWith;
	private Vertex end;
	private final Direction d;
	private final Compaction c;
	private TurnPriority turnPriority;
	private double length;
	private final boolean otherSide;
	private boolean fixedLength;

	public boolean isFixedLength() {
		return fixedLength;
	}

	public void setFixedLength(boolean fixedLength) {
		this.fixedLength = fixedLength;
	}

	public boolean isOtherSide() {
		return otherSide;
	}

	public double getLength(boolean recalculate) {
		if (recalculate) {
			double newValue = getEarly().minimumDistanceTo(getLate());
			if (newValue != length) {
				if (fixedLength) {
					throw new LogicException("Length shouldn't change!");
				}
				
				length = newValue;
			}
		}
		
		return length;
	}

	public TurnPriority getTurnPriority() {
		return turnPriority;
	}

	public Compaction getCompaction() {
		return c;
	}

	public Segment getSegment() {
		return s.getUnderlying();
	}
	
	public Slideable<Segment> getSlideable() {
		return s;
	}
	
	public String toString() {
		return "["+number+" "+turnPriority+"\n     s="+s.getUnderlying()+"\n  from="+startsWith+"\n    to="+endsWith+"\n     d="+d+"\n]";
	}
	
	public Slideable<Segment> getLate() {
		switch (d) {
		case UP:
		case LEFT:
			return startsWith;
		case DOWN:
		case RIGHT:
		default:
			return endsWith;
		}
	}

	public Slideable<Segment> getEarly() {
		switch (d) {
		case UP:
		case LEFT:
			return endsWith;
		case DOWN:
		case RIGHT:
		default:
			return startsWith;
		}
	}

	public boolean isLengthKnown() {
		if ((getEarly().getMaximumPosition() == null) || (getLate().getMaximumPosition() == null)) {
			return false;
		}
		
		int earlySpan = getEarly().getMaximumPosition() - getEarly().getMinimumPosition();
		int lateSpan = getLate().getMaximumPosition() - getLate().getMinimumPosition();
		
		return (earlySpan == lateSpan);
	}
	

	public Slideable<Segment> getStartsWith() {
		return startsWith;
	}

	public Slideable<Segment> getEndsWith() {
		return endsWith;
	}

	public Direction getDirection() {
		return d;
	}

	public void resetEndsWith(Slideable<Segment> s, TurnPriority tp, double minLength) {
		this.endsWith = s;
		this.end = null;
		this.turnPriority = TurnPriority.values()[Math.max(tp.ordinal(), turnPriority.ordinal())];
		this.length = Math.max(0, minLength);
	}

	public void resetStartsWith(Slideable<Segment> s, TurnPriority tp, double minLength) {
		this.startsWith = s;
		this.start = null;
		this.turnPriority = TurnPriority.values()[Math.max(tp.ordinal(), turnPriority.ordinal())];
		this.length = Math.max(0, minLength);
	}

	public void ensureMinLength(double l) {
		Slideable<Segment> early = getEarly();
		Slideable<Segment> late = getLate();
		early.getSlackOptimisation().ensureMinimumDistance(early, late, (int) l);
		System.out.println(this+": "+l);
		this.length = l;
	}
	
	public void ensureMaxLength(double l) {
		Slideable<Segment> early = getEarly();
		Slideable<Segment> late = getLate();
		early.getSlackOptimisation().ensureMaximumDistance(early, late, (int) l);
	}
	
	public boolean isFanTurn(VertexTurn atEnd) {
		if (isFanTurn()) {
			if (atEnd.s == startsWith) {
				return isStartInnerFan();
			} else if (atEnd.s == endsWith) {
				return isEndInnerFan();
			} else {
				throw new Kite9ProcessingException();
			}
		}
		
		return false;
		
	}
	
	/**
	 * The order of deciding this is important, since a segment 
	 * can be shared by a label.
	 */
	protected TurnPriority calculateTurnPriority() {
		if (isConnection()) {
			return TurnPriority.CONNECTION;
		} else if (isMaximizeRectangular()) {
			return TurnPriority.MAXIMIZE_RECTANGULAR;
		} else if (isMinimizeRectangular()) {
			return TurnPriority.MINIMIZE_RECTANGULAR;
		} else {
			return TurnPriority.CONNECTION;	// layout connection?
		}
	}

	private boolean isFanTurn() {
		return (start instanceof FanVertex) && (end instanceof FanVertex);
	}
	
	public FanVertex getInnerFanVertex() {
		if (isEndInnerFan()) {
			return (FanVertex) end;
		} else if (isStartInnerFan()) {
			return (FanVertex) start;
		} else {
			return null;
		}
	}

	private boolean isEndInnerFan() {
		return (end instanceof FanVertex) && ((FanVertex)end).isInner();
	}

	private boolean isStartInnerFan() {
		return (start instanceof FanVertex) && ((FanVertex)start).isInner();
	}
	
	public boolean isMinimizeRectangleBounded() {
		long rectsAtBothEnds = getUnderlyingsOfType(startsWith, Rectangular.class)
				.filter(de -> endsWith.getUnderlying().hasUnderlying(de))
				.filter(minimize()).count();
		return rectsAtBothEnds > 0;
	}
	
	public boolean isConnectionBounded() {
		long startsWithConnections = getUnderlyingsOfType(startsWith, Connection.class).count();
		long endsWithConnections = getUnderlyingsOfType(endsWith, Connection.class).count();
		
		return (startsWithConnections > 0) && (endsWithConnections > 0);
	}
	
	public boolean isMinimizeRectangleCorner() {
		long rects = getUnderlyingsOfType(s, Rectangular.class)
			.filter(de -> endsWith.getUnderlying().hasUnderlying(de) || startsWith.getUnderlying().hasUnderlying(de))
			.filter(minimize()).count();
		
	
		return rects > 0;
	}

	private Predicate<? super Rectangular> minimize() {
		return r -> (r.getSizing() == DiagramElementSizing.MINIMIZE);
	}
	
	private Predicate<? super Rectangular> maximize() {
		return r -> (r.getSizing() == DiagramElementSizing.MAXIMIZE);
	}

	public boolean isConnection() {
		long connections = getUnderlyingsOfType(s, Connection.class).count();
		return connections > 0;
	}
	
	public boolean isMinimizeRectangular() {
		long rects = getUnderlyingsOfType(s, Rectangular.class).filter(minimize()).count();
		return rects > 0;
	}
	
	public boolean isMaximizeRectangular() {
		long rects = getUnderlyingsOfType(s, Rectangular.class).filter(maximize()).count();
		return rects > 0;
	}
	
	@SuppressWarnings("unchecked")
	private static <X extends DiagramElement> Stream<X> getUnderlyingsOfType(Slideable<Segment> s, Class<X> c) {
		return s.getUnderlying().getUnderlyingInfo().stream().map(ui -> ui.getDiagramElement()).filter(de -> c.isAssignableFrom(de.getClass())).map(de -> (X) de);
	}
	
	public Set<Connection> getLeavingConnections() {
		Set<Connection> leavingConnections = getSegment().getAdjoiningSegments(c).stream()
			.flatMap(seg -> seg.getConnections().stream())
			.collect(Collectors.toSet());
			
		return leavingConnections;
	}
	

}