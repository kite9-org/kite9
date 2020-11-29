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
import org.kite9.diagram.model.Label;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.SizedRectangular;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.style.DiagramElementSizing;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.segment.Segment;
import org.kite9.diagram.visualization.compaction.segment.Side;
import org.kite9.diagram.visualization.compaction.slideable.SegmentSlackOptimisation;
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
	
	public VertexTurn(int number, Compaction c, Slideable<Segment> s, Direction d, Slideable<Segment> startsWith, Slideable<Segment> endsWith, Rectangular partOf) {
		this.d = d;
		this.s = s;
		this.startsWith = startsWith;
		this.endsWith = endsWith;
		this.partOf = partOf;
		this.start = commonVertex(s.getUnderlying(), startsWith.getUnderlying());
		this.end = commonVertex(s.getUnderlying(), endsWith.getUnderlying());
		this.number = number;
		this.c = c;
		this.turnPriority = calculateTurnPriority(s);
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
	private boolean fixedLength;
	private final Rectangular partOf;

	/**
	 * Returns true if the turn in question cannot be expanded due to a rectangle getting bigger
	 * Questionable usefulness.
	 * @return
	 */
	public boolean isNonExpandingLength() {
		return fixedLength;
	}

	public void setNonExpandingLength(boolean fixedLength) {
		this.fixedLength = fixedLength;
	}

	/**
	 * getLength now considers that some elements need to be minimized.  If it starts/ends with a 
	 * MINIMIZE section, it will calculate to the other end of this
	 * @param recalculate
	 * @return
	 */
	public double getLength(boolean recalculate) {
		if (recalculate) {
			double newValue = recalculateLength();
			if (newValue != length) {
				//if (fixedLength) {
				//	throw new LogicException("Length shouldn't change!");
				//}
				
				length = newValue;
			}
		}
		
		return length;
	}
	
	private double recalculateLength() {
		Slideable<Segment> startUse = checkMinimizeSlideable(startsWith, true);
		Slideable<Segment> endUse = checkMinimizeSlideable(endsWith, false);
		
		boolean increasing = increasingDirection();
		double mainDistance = increasing ? startUse.minimumDistanceTo(endUse) : endUse.minimumDistanceTo(startUse);
		double earlyReduction = 0;
		double lateReduction = 0;
		
		
		if (startUse != startsWith) {
			earlyReduction = increasing ? startUse.minimumDistanceTo(startsWith) : startsWith.minimumDistanceTo(startUse);
		}
		
		if (endUse != endsWith) {
			lateReduction = increasing ? endsWith.minimumDistanceTo(endUse) : endUse.minimumDistanceTo(endsWith);
		}
		
		return mainDistance - earlyReduction - lateReduction;
		
	}
	
	/**
	 * When given a vertexTurn which is a subset of a whole length, this can extend the 
	 * length to the whole slideable in order that we can calculate a length based on the whole thing (?)
	 */
	private Slideable<Segment> checkMinimizeSlideable(Slideable<Segment> orig, boolean from) {
		if (calculateTurnPriority(orig)!=TurnPriority.MINIMIZE_RECTANGULAR) {
			return orig;
		}
		
		boolean maxTo = increasingDirection();
		Side needed = (maxTo == from) ? Side.START : Side.END;
		
		Side current = orig.getUnderlying().getSingleSide();
		
		if ((current == needed) || (current == Side.BOTH)) {
			return orig;
		}
		
		Set<Rectangular> rects = orig.getUnderlying().getRectangulars();
		Rectangular rect = getTopmostRectangular(rects);
		Slideable<Segment> out = ((SegmentSlackOptimisation) orig.getSlackOptimisation()).getSlideablesFor(rect).otherOne(orig);
		return out;
	}

	public Rectangular getTopmostRectangular(Set<Rectangular> rects) {
		if (rects.size() == 0) {
			throw new LogicException("Couldn't determine underlying");
		}
		Rectangular rect = rects.stream().reduce((r1, r2) -> {
				if (r1.getDepth() > r2.getDepth()) {
					return r2;
				} else if (r2.getDepth() > r1.getDepth()) {
					return r1;
				} else {
					return r1;
				} 
			}).orElseThrow(() -> new LogicException("Couldn't determine underlying"));
		return rect;
	}

	public boolean increasingDirection() {
		return (getDirection() == Direction.DOWN) || (getDirection() == Direction.RIGHT);
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
		return "["+number+" "+turnPriority+"\n     s="+s+"\n  from="+startsWith+"\n    to="+endsWith+"\n     d="+d+"\n]";
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
		Slideable<Segment> early = increasingDirection() ? getStartsWith() : getEndsWith();
		Slideable<Segment> late = increasingDirection() ? getEndsWith() : getStartsWith();
		early.getSlackOptimisation().ensureMinimumDistance(early, late, (int) l);
		this.length = l;
	}
	
	public boolean isFanTurn(VertexTurn atEnd) {
		if (isFanTurn()) {
			if (atEnd.s == startsWith) {
				return isStartInnerFan();
			} else if (atEnd.s == endsWith) {
				return isEndInnerFan();
			} else {
				throw new LogicException();
			}
		}
		
		return false;
		
	}
	
	/**
	 * The order of deciding this is important, since a segment 
	 * can be shared by a label.
	 */
	public TurnPriority calculateTurnPriority(Slideable<Segment> s) {
		if (isConnection(s)) {
			return TurnPriority.CONNECTION;
		} else if (isMaximizeRectangular(s)) {
			return TurnPriority.MAXIMIZE_RECTANGULAR;
		} else if (isMinimizeRectangular(s)) {
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
	
	public boolean isContainerLabelOnSide(Direction d) {
		long labels = getUnderlyingsOfType(s, Label.class)
				.filter(l -> !l.isConnectionLabel())
				.filter(l -> ((Label) l).getLabelPlacement().sameAxis(d))
				.count();;
				
		return (labels > 0);				
	}
	
	private Predicate<? super Rectangular> minimize(boolean horiz) {
		return r -> (r instanceof SizedRectangular) && (((SizedRectangular) r).getSizing(horiz) == DiagramElementSizing.MINIMIZE) && (r != partOf);
	}
	
	private Predicate<? super Rectangular> maximize(boolean horiz) {
		return r -> ((r instanceof SizedRectangular) && (((SizedRectangular) r).getSizing(horiz) == DiagramElementSizing.MAXIMIZE)) || (r == partOf);
	}

	public static boolean isConnection(Slideable<Segment> s) {
		long connections = getUnderlyingsOfType(s, Connection.class).count();
		return connections > 0;
	}
	
	public boolean isMinimizeRectangular(Slideable<Segment> s) {
		long rects = getUnderlyingsOfType(s, Rectangular.class)
			.filter(minimize(Direction.isHorizontal(d))).count();
		return rects > 0;
	}
	
	public boolean isMaximizeRectangular(Slideable<Segment> s) {
		long rects = getUnderlyingsOfType(s, Rectangular.class)
			.filter(maximize(Direction.isHorizontal(d))).count();
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

	public Rectangular getPartOf() {
		return partOf;
	}
	
	

}