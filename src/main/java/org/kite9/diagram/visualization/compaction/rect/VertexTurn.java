package org.kite9.diagram.visualization.compaction.rect;

import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.Segment;
import org.kite9.diagram.visualization.orthogonalization.Dart;

/**
 * Stores the segments on the stack 
 */
class VertexTurn {
	
	private Compaction c;

	public VertexTurn(Segment s, Compaction c, Direction d) {
		this.segment = s;
		this.c = c;
		this.d = d;
	}
	
	private Segment segment;
	
	public Segment getSegment() {
		return segment;
	}

	Vertex startsWith;
	Vertex endsWith;
	
	Direction d;
	
	public String toString() {
		return "["+number+"]"+startsWith+" "+d+" "+((underlying==null) ? "" : underlying.getChangeCost())+ " ("+segment+")";
	}
		
	int number;
	
	private Dart underlying;
	double tempLength;
	int tempChangeCost;
	
	public Dart getUnderlying() {
		if (underlying == null) {
			underlying = c.getOrthogonalization().createDart(startsWith, endsWith, null, d, 0);
			underlying.setChangeCost(tempChangeCost, null);
			return underlying;
		} else {
			return underlying;
		}
	}
	
	public void setUnderlying(Dart d) {
		this.underlying = d;
	}
}