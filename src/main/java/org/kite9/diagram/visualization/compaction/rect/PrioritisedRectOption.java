package org.kite9.diagram.visualization.compaction.rect;

import java.util.List;

import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.compaction.rect.PrioritizingRectangularizer.Match;
import org.kite9.diagram.visualization.compaction.rect.VertexTurn.TurnPriority;

public class PrioritisedRectOption extends RectOption {
	
	static enum TurnType {
		
		CONNECTION_FAN(-10000),
		EXTEND_IF_NEEDED(10000), 
		MINIMIZE_RECT_SIDE(30000), 		// whole side of rectangular
		MINIMIZE_RECT_SIDE_PART(30000),    // connection-to-connection of rectangular 
		CONNECTION_NORMAL(40000),
		MINIMIZE_RECT_CORNER(50000),		// connection-to-corner of rectangular
		MINIMIZE_RECT_CORNER_SINGLE(60000),		// when there is a single connection on the corner
		SAFE(100000);

		private TurnType(int c) {
			this.cost = c;
		}
		
		private final int cost;

		public int getCost() {
			return cost;
		}
	};

	private TurnType type;
	
	public PrioritisedRectOption(int i, VertexTurn vt1, VertexTurn vt2, VertexTurn vt3, VertexTurn vt4, VertexTurn vt5, Match m, List<VertexTurn> fromStack) {
		super(i, vt1, vt2, vt3, vt4, vt5, m, fromStack);
		this.type = getType();
		this.initialScore = getScore();
	}

	/**
	 * Lower scores are better rect options
	 */
	public int getScore() {
		// do safe ones last
		int pushOut = calculatePushOut();
		int typeCost = type.getCost();
		return pushOut + typeCost;
	}
	
	private int calculatePushOut() {
		VertexTurn par = getPar();
		VertexTurn meets = getMeets();
		
		TurnPriority tp = meets.calculateTurnPriority();
		
		double parLength = par.getMinimumLength();
		double meetsLength = meets.getMinimumLength();
		
		int distance = (int) Math.max(0,parLength - meetsLength);
		return distance * tp.getCostFactor();
	}

	public TurnType getType() {
		VertexTurn extender = getExtender();
		VertexTurn meets = getMeets();
		VertexTurn par = getPar();
		if (extender.isFanTurn(par)) {
			Direction parDirection = getTurnDirection(par);
			List<Direction> fanDirections = extender.getInnerFanVertex().getFanSides();
			if (fanDirections.get(0) == parDirection) {
				return TurnType.CONNECTION_FAN;
			}
		}
		
		if (isConcave()) {
			return TurnType.SAFE;
		}

		switch (meets.getTurnPriority()) {
		case CONNECTION:
			return TurnType.CONNECTION_NORMAL;

		case MINIMIZE_RECTANGULAR:
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
		case MAXIMIZE_RECTANGULAR:
		default:
			return TurnType.EXTEND_IF_NEEDED;
		}
	}
	
	
	
	public boolean isConcave() {
		return getPost().getDirection() == getExtender().getDirection();
	}
	
	public String toString() {
		return "\n[RO: "+i+"("+this.getInitialScore()+")"+ ", meetsType = "+type+ ", extender = " + getExtender().getSegment() +"]"; 
	}
}
