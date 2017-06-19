package org.kite9.diagram.visualization.compaction.rect;

import java.util.List;

import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.rect.PrioritizingRectangularizer.Match;

public class PrioritisedRectOption extends RectOption {
	
	static enum TurnType {
		
		EXTEND_IF_NEEDED(0), 
		MINIMIZE_RECT_CORNER(50000), 		// connection-to-corner of rectangular
		MINIMIZE_RECT_SIDE(30000), 		// whole side of rectangular
		MINIMIZE_RECT_SIDE_PART(30000),    // connection-to-connection of rectangular 
		CONNECTION_NORMAL(40000),
		CONNECTION_FAN(10000);

		private TurnType(int c) {
			this.cost = c;
		}
		
		private final int cost;

		public int getCost() {
			return cost;
		}
	};

	private TurnType meetsType;
	
	public PrioritisedRectOption(int i, PrioritizingRectangularizer prioritizingRectangularizer, VertexTurn vt1, VertexTurn vt2, VertexTurn vt3, VertexTurn vt4, VertexTurn vt5, Match m, Compaction c, List<VertexTurn> fromStack) {
		super(i, vt1, vt2, vt3, vt4, vt5, m, fromStack);
		this.meetsType = getType(getMeets());
	}

	/**
	 * Lower scores are better rect options
	 */
	public int getScore() {
		VertexTurn extender = getExtender();
		if (extender.isFanTurn(getPar())) {
			return -10;		// priority for closing fans
		} 
		
		// do safe ones last
		int safeCost = isSizingSafe() ? 100000 : 0;
		int pushOut = calculatePushOut();
		
		TurnType mt = getType(getMeets());
		int meetsCost = mt.getCost();
		
		return pushOut + meetsCost + safeCost;
	}
	
	private int calculatePushOut() {
		VertexTurn par = getPar();
		VertexTurn meets = getMeets();
		
		double parLength = par.getMinimumLength();
		double meetsLength = meets.getMinimumLength();
		
		return (int) Math.max(0,parLength - meetsLength);
	}

	public TurnType getType(VertexTurn meets) {
		if (meets.isConnection()) {
			if (meets.isFanTurn(null)) {
				return TurnType.CONNECTION_FAN;
			} else {
				return TurnType.CONNECTION_NORMAL;
			}
		} else if (meets.isMinimizeRectangular()) {
			if (meets.isConnectionBounded()) {
				return TurnType.MINIMIZE_RECT_SIDE_PART;
			} else if (meets.isMinimizeRectangleCorner()) {
				return TurnType.MINIMIZE_RECT_CORNER;
			} else if (meets.isConnectionBounded()) {
				return TurnType.MINIMIZE_RECT_SIDE_PART;
			} else {
				return TurnType.EXTEND_IF_NEEDED;
			}
		} else {
			return TurnType.EXTEND_IF_NEEDED;
		}
	}
	
	public String toString() {
		return "[RO: ("+this.getInitialScore()+")"+ ", safe = "+isSizingSafe()+", meetsType = "+meetsType+ ", extender = " + getExtender().getSegment() +"]"; 
	}
}
