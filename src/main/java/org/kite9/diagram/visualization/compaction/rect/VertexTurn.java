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
	
	public VertexTurn(Compaction c, Slideable s, Direction d, int changeCost, Slideable startsWith, Slideable endsWith, boolean changeEarlyStart, boolean changeEarlyEnd) {
		this.d = d;
		this.s = s;
		this.changeCost = changeCost;
		this.changeEarlyStart = changeEarlyStart;
		this.changeEarlyEnd = changeEarlyEnd;
		
		this.startsWith = startsWith;
		this.endsWith = endsWith;
	}
	
	public VertexTurn(Compaction c, Slideable s, Direction d, int changeCost, Vertex startsWith, Vertex endsWith, boolean changeEarlyStart, boolean changeEarlyEnd) {
		this(c, s, d, changeCost,  getSlideableForVertex(c, d, startsWith), 
				getSlideableForVertex(c, d, endsWith), changeEarlyStart, changeEarlyEnd);
	}

	private static Slideable getSlideableForVertex(Compaction c, Direction d, Vertex startsWith) {
		return c.getSlackOptimisation(Direction.rotateClockwise(d)).getVertexToSlidableMap().get(startsWith);
	}
	
	private Slideable s;
	private int number;
	private int changeCost;
	private Slideable startsWith;
	private Slideable endsWith;
	private Direction d;
	private boolean changeEarlyStart;
	private boolean changeEarlyEnd;
	
	public boolean isChangeEarlyStart() {
		return changeEarlyStart;
	}

	public boolean isChangeEarlyEnd() {
		return changeEarlyEnd;
	}

	
	public Segment getSegment() {
		return (Segment) s.getUnderlying();
	}
	
	public Slideable getSlideable() {
		return s;
	}
	
	public String toString() {
		return "["+number+"\n     s="+s.getUnderlying()+"\n  from="+startsWith.getUnderlying()+"\n    to="+endsWith.getUnderlying()+"\n     d="+d+"\n]";
	}
	
	public int getChangeCost() {
		return changeCost;
	}
	
	public int getMinimumLength() {
		return getEarly().minimumDistanceTo(getLate());
	}
	
	private Slideable getLate() {
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

	private Slideable getEarly() {
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
	

	public Slideable getStartsWith() {
		return startsWith;
	}

	public Slideable getEndsWith() {
		return endsWith;
	}

	public Direction getDirection() {
		return d;
	}

	public void resetEndsWith(Compaction c, Vertex to, boolean changeEarly, int changeCost) {
		resetEndsWith(getSlideableForVertex(c, d, to), changeEarly, changeCost);
	}
		
	public void resetEndsWith(Slideable s, boolean changeEarly, int changeCost) {
		this.endsWith = s;
		this.changeEarlyEnd = changeEarly;
		this.changeCost = Math.max(changeCost, this.changeCost);
	}
	
	public void resetStartsWith(Compaction c, Vertex to, boolean changeEarly, int changeCost) {
		resetStartsWith(getSlideableForVertex(c, d, to), changeEarly, changeCost);
	}
		
	public void resetStartsWith(Slideable s, boolean changeEarly, int changeCost) {
		this.startsWith = s;
		this.changeEarlyStart = changeEarly;
		this.changeCost = Math.max(changeCost, this.changeCost);
	}

	public void ensureLength(double l) {
		Slideable early = getEarly();
		Slideable late = getLate();
		System.out.println("from " +early);
		System.out.println("to   " +late);

		early.getSlackOptimisation().ensureMinimumDistance(early, late, (int) l);
	}

}