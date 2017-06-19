package org.kite9.diagram.visualization.compaction.rect;

import java.util.Optional;
import java.util.function.Predicate;
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

/**
 * Stores the segments on the stack 
 */
class VertexTurn {
	
	public VertexTurn(int number, Compaction c, Slideable<Segment> s, Direction d, Slideable<Segment> startsWith, Slideable<Segment> endsWith) {
		this.d = d;
		this.s = s;
		this.startsWith = startsWith;
		this.endsWith = endsWith;
		this.start = commonVertex(s.getUnderlying(), startsWith.getUnderlying());
		this.end = commonVertex(s.getUnderlying(), endsWith.getUnderlying());
		this.number = number;
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

	private final Slideable<Segment> s;
	private final int number;
	private Slideable<Segment> startsWith;
	private Vertex start;
	private Slideable<Segment> endsWith;
	private Vertex end;
	private final Direction d;
		
	public Segment getSegment() {
		return s.getUnderlying();
	}
	
	public Slideable<Segment> getSlideable() {
		return s;
	}
	
	public String toString() {
		return "["+number+"\n     s="+s.getUnderlying()+"\n  from="+startsWith.getUnderlying()+"\n    to="+endsWith.getUnderlying()+"\n     d="+d+"\n]";
	}
	
	public int getMinimumLength() {
		return getEarly().minimumDistanceTo(getLate());
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

	public void resetEndsWith(Slideable<Segment> s) {
		this.endsWith = s;
		this.end = null;
	}

	public void resetStartsWith(Slideable<Segment> s) {
		this.startsWith = s;
		this.start = null;
	}

	public void ensureMinLength(double l) {
		Slideable<Segment> early = getEarly();
		Slideable<Segment> late = getLate();
		early.getSlackOptimisation().ensureMinimumDistance(early, late, (int) l);
	}
	
	public void ensureMaxLength(double l) {
		Slideable<Segment> early = getEarly();
		Slideable<Segment> late = getLate();
		early.getSlackOptimisation().ensureMaximumDistance(early, late, (int) l);
	}
	
	public boolean isFanTurn(VertexTurn atEnd) {
		if ((start instanceof FanVertex) && (end instanceof FanVertex)) {
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

	private boolean isEndInnerFan() {
		return (end instanceof FanVertex) && ((FanVertex)end).isInner();
	}

	private boolean isStartInnerFan() {
		return (start instanceof FanVertex) && ((FanVertex)start).isInner();
	}
	
	public boolean isMinimizeRectangleBounded(Rectangular exclude) {
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

	public boolean isConnection() {
		long connections = getUnderlyingsOfType(s, Connection.class).count();
		return connections > 0;
	}
	
	public boolean isMinimizeRectangular() {
		long rects = getUnderlyingsOfType(s, Rectangular.class).filter(minimize()).count();
		return rects > 0;
	}
	
	@SuppressWarnings("unchecked")
	private static <X extends DiagramElement> Stream<X> getUnderlyingsOfType(Slideable<Segment> s, Class<X> c) {
		return s.getUnderlying().getUnderlyingInfo().stream().map(ui -> ui.getDiagramElement()).filter(de -> c.isAssignableFrom(de.getClass())).map(de -> (X) de);
	}
	
	public boolean isFixedLength() {
		return (getEarly().hasMaximumConstraints()) || (getLate().hasMaximumConstraints());
	}
}