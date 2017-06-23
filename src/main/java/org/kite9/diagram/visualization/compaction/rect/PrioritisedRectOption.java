package org.kite9.diagram.visualization.compaction.rect;

import java.util.List;

import org.kite9.diagram.model.Connected;
import org.kite9.diagram.visualization.compaction.rect.PrioritizingRectangularizer.Match;

public class PrioritisedRectOption extends RectOption {
	
	static enum TurnType {
		
		CONNECTION_FAN(-10000),
		EXTEND_IF_NEEDED(0), 
		MINIMIZE_RECT_SIDE(30000), 		// whole side of rectangular
		MINIMIZE_RECT_SIDE_PART(30000),    // connection-to-connection of rectangular 
		CONNECTION_NORMAL(40000),
		MINIMIZE_RECT_CORNER(50000),		// connection-to-corner of rectangular
		MINIMIZE_RECT_CORNER_SINGLE(60000);		// when there is a single connection on the corner

		private TurnType(int c) {
			this.cost = c;
		}
		
		private final int cost;

		public int getCost() {
			return cost;
		}
	};

	private TurnType type;
	private boolean sizeSafe; 
	
	public PrioritisedRectOption(int i, VertexTurn vt1, VertexTurn vt2, VertexTurn vt3, VertexTurn vt4, VertexTurn vt5, Match m, List<VertexTurn> fromStack) {
		super(i, vt1, vt2, vt3, vt4, vt5, m, fromStack);
		this.type = getType();
		this.sizeSafe = isSizingSafe();
		this.initialScore = getScore();
	}

	/**
	 * Lower scores are better rect options
	 */
	public int getScore() {
		// do safe ones last
		int safeCost = sizeSafe ? 100000 : 0;
		int pushOut = sizeSafe ? 0 : calculatePushOut();
		int typeCost = type.getCost();
		return pushOut + typeCost + safeCost;
	}
	
	private int calculatePushOut() {
		VertexTurn par = getPar();
		VertexTurn meets = getMeets();
		
		double parLength = par.getMinimumLength();
		double meetsLength = meets.getMinimumLength();
		
		return (int) Math.max(0,parLength - meetsLength);
	}

	public TurnType getType() {
		VertexTurn extender = getExtender();
		VertexTurn meets = getMeets();
		if (extender.isFanTurn(getPar())) {
			Connected c = extender.getInnerFanVertex().getFanForEnd();
			if (getLink().getSegment().hasUnderlying(c)) {
				return TurnType.CONNECTION_FAN;
			}
		}
		
		if (meets.isConnection()) {
			return TurnType.CONNECTION_NORMAL;
		} else if (meets.isMinimizeRectangular()) {
			if (meets.isConnectionBounded()) {
				return TurnType.MINIMIZE_RECT_SIDE_PART;
			} else if (meets.isMinimizeRectangleCorner()) {
				if (meets.getLeavingConnections().size() == 1) {
					return TurnType.MINIMIZE_RECT_CORNER_SINGLE;
				}
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
	
	/**
	 * This basically says whether we can rectangularize without causing meets to grow IF par is shorter 
	 * or the same length as meets.
	 */
	public boolean isSizingSafe() {
		return type != TurnType.CONNECTION_FAN && (getPost().getDirection() == getExtender().getDirection());
	}
	
	public String toString() {
		return "\n[RO: "+i+"("+this.getInitialScore()+")"+ ", safe = "+sizeSafe+", meetsType = "+type+ ", extender = " + getExtender().getSegment() +"]"; 
	}
}
