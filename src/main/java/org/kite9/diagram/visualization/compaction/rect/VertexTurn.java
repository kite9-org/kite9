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
	
	public VertexTurn(Compaction c, Slideable s, Direction d, int changeCost, Vertex startsWith, Vertex endsWith, boolean changeEarlyStart, boolean changeEarlyEnd) {
		this.d = d;
		this.s = s;
		this.changeCost = changeCost;
		this.changeEarlyStart = changeEarlyStart;
		this.changeEarlyEnd = changeEarlyEnd;
		
		this.startsWith = c.getSlackOptimisation(d).getVertexToSlidableMap().get(startsWith);
		this.endsWith = c.getSlackOptimisation(d).getVertexToSlidableMap().get(endsWith);
		
		switch (d) {
		case DOWN:
		case RIGHT:
			early = this.startsWith;
			late = this.endsWith;
			break;
		case UP:
		case LEFT:
			late = this.endsWith;
			early = this.startsWith;
			break;
		}
	}
	
	private Slideable s;
	private int number;
	private int changeCost;
	private Slideable startsWith;
	private Slideable endsWith;
	private Direction d;
	private boolean changeEarlyStart;
	private boolean changeEarlyEnd;
	private Slideable early;
	private Slideable late;
	
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
		return "["+number+"]"+startsWith+" - "+endsWith+" - "+d+" "+ " ("+getSegment()+")";
	}
	
	public int getChangeCost() {
		return changeCost;
	}
	
	public int getMinimumLength() {
		return early.minimumDistanceTo(late);
	}
	
	public boolean isLengthKnown() {
		if ((early.getMaximumPosition() == null) || (late.getMaximumPosition() == null)) {
			return false;
		}
		
		int earlySpan = early.getMaximumPosition() - early.getMinimumPosition();
		int lateSpan = late.getMaximumPosition() - late.getMinimumPosition();
		
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
		this.endsWith = c.getSlackOptimisation(d).getVertexToSlidableMap().get(endsWith);
		this.changeEarlyEnd = changeEarly;
		this.changeCost = Math.max(changeCost, this.changeCost);
	}

}