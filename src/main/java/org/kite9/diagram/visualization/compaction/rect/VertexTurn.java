package org.kite9.diagram.visualization.compaction.rect;

import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.segment.Segment;

/**
 * Stores the segments on the stack 
 */
class VertexTurn {
	
	public VertexTurn(Compaction c, Slideable<Segment> s, Direction d, Slideable<Segment> startsWith, Slideable<Segment> endsWith) {
		this.d = d;
		this.s = s;
		this.startsWith = startsWith;
		this.endsWith = endsWith;
	}
	
	public VertexTurn(Compaction c, Slideable<Segment> s, Direction d, Vertex startsWith, Vertex endsWith) {
		this(c, s, d, getSlideableForVertex(c, d, startsWith), 
				getSlideableForVertex(c, d, endsWith));
	}

	private static Slideable<Segment> getSlideableForVertex(Compaction c, Direction d, Vertex startsWith) {
		return c.getSlackOptimisation(!Direction.isHorizontal(d)).getVertexToSlidableMap().get(startsWith);
	}
	
	private Slideable<Segment> s;
	private int number;
	private Slideable<Segment> startsWith;
	private Slideable<Segment> endsWith;
	private Direction d;
		
	public Segment getSegment() {
		return (Segment) s.getUnderlying();
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
	
	private Slideable<Segment> getLate() {
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

	private Slideable<Segment> getEarly() {
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

	public void resetEndsWith(Compaction c, Vertex to) {
		resetEndsWith(getSlideableForVertex(c, d, to));
	}
		
	public void resetEndsWith(Slideable<Segment> s) {
		this.endsWith = s;
	}

	public void resetStartsWith(Compaction c, Vertex to) {
		resetStartsWith(getSlideableForVertex(c, d, to));
	}
		
	public void resetStartsWith(Slideable<Segment> s) {
		this.startsWith = s;
	}

	public void ensureLength(double l) {
		Slideable<Segment> early = getEarly();
		Slideable<Segment> late = getLate();
		early.getSlackOptimisation().ensureMinimumDistance(early, late, (int) l);
	}
	
	public int getChangeCost() {
		return 1;
	}

}