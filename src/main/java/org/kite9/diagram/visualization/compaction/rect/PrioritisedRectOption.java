package org.kite9.diagram.visualization.compaction.rect;

import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.rect.PrioritizingRectangularizer.Match;
import org.kite9.framework.common.Kite9ProcessingException;

public class PrioritisedRectOption extends RectOption {
	
	static enum MeetsType { 
		EXTEND_IF_NEEDED, 
		MINIMIZE_RECT_CORNER, 		// connection-to-corner of rectangular
		MINIMIZE_RECT_SIDE, 		// whole side of rectangular
		MINIMIZE_RECT_SIDE_PART,    // connection-to-connection of rectangular 
		CONNECTION_NORMAL,
		CONNECTION_FAN };

	private Rectangular partOf;
	private MeetsType meetsType;
	
	public PrioritisedRectOption(int i, PrioritizingRectangularizer prioritizingRectangularizer, VertexTurn vt1, VertexTurn vt2, VertexTurn vt3, VertexTurn vt4, VertexTurn vt5, Match m, Compaction c, Rectangular partOf) {
		super(i, vt1, vt2, vt3, vt4, vt5, m);
		this.partOf = partOf;
		this.meetsType = getMeetsType();
	}

	/**
	 * Lower scores are better rect options
	 */
	public int getScore() {
		VertexTurn extender = getExtender();
		
		if (extender.isFanTurn(getPar())) {
			return -10;		// priority for closing fans
		} 

		MeetsType mt = getMeetsType();
		switch (mt) {
		case EXTEND_IF_NEEDED:
			return 0;
		case CONNECTION_FAN:
			return 1;
		case MINIMIZE_RECT_SIDE:
			return 3;
		case MINIMIZE_RECT_SIDE_PART:
			return 3;
		case CONNECTION_NORMAL:
			return 4;
		case MINIMIZE_RECT_CORNER:
			return 5;
		default: 
			return 0;
		}
	}
	
	public MeetsType getMeetsType() {
		VertexTurn meets = getMeets();
		if (meets.isConnection()) {
			if (meets.isFanTurn(null)) {
				return MeetsType.CONNECTION_FAN;
			} else {
				return MeetsType.CONNECTION_NORMAL;
			}
		} else if (meets.isMinimizeRectangular(partOf)) {
			if (meets.isConnectionBounded()) {
				return MeetsType.MINIMIZE_RECT_SIDE_PART;
			} else if (meets.isMinimizeRectangleCorner(partOf)) {
				return MeetsType.MINIMIZE_RECT_CORNER;
			} else if (meets.isConnectionBounded()) {
				return MeetsType.MINIMIZE_RECT_SIDE_PART;
			} else {
				return MeetsType.EXTEND_IF_NEEDED;
			}
		} else {
			return MeetsType.EXTEND_IF_NEEDED;
		}
	}
	
	public String toString() {
		return "[RO: ("+this.getInitialScore()+")"+ ", meetsType = "+meetsType+ " extender = " + getExtender().getSegment() +"]"; 
	}
}
