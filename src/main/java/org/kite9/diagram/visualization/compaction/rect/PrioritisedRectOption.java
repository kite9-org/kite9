package org.kite9.diagram.visualization.compaction.rect;

import java.util.List;

import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.compaction.rect.PrioritizingRectangularizer.Match;
import org.kite9.diagram.visualization.compaction.rect.VertexTurn.TurnPriority;
import org.kite9.framework.logging.LogicException;

public class PrioritisedRectOption extends RectOption {
	
	/**
	 * Refers to whether the option turns back on itself (G), or continues the way it was going (U).
	 * @author robmoffat
	 *
	 */
	static enum TurnShape {
		U, G;		
	}
	
	static enum TurnType {
		
		CONNECTION_FAN(-10000, null),
		EXTEND_PREFERRED(0, TurnShape.G),
		EXTEND_IF_NEEDED(10000, TurnShape.G), 
		MINIMIZE_RECT_SIDE(30000, TurnShape.G), 		// whole side of rectangular
		MINIMIZE_RECT_SIDE_PART(30000, TurnShape.G),    // connection-to-connection of rectangular 
		CONNECTION_NORMAL(40000,TurnShape.G),
		MINIMIZE_RECT_INSIDE_CORNER(50000, TurnShape.G),		// connection-to-corner of rectangular
		SAFE(100000, TurnShape.U), 
		SAFE_BUT_POOR(110000, TurnShape.U),
		MINIMIZE_RECT_OUTSIDE_CORNER(50000, TurnShape.U),		
		MINIMIZE_RECT_OUTSIDE_CORNER_SINGLE(60000, TurnShape.U);		

		private TurnType(int c, TurnShape shape) {
			this.cost = c;
			this.shape = shape;
		}
		
		private final int cost;
		private final TurnShape shape;

		public int getCost() {
			return cost;
		}
		
		public TurnShape getTurnShape() {
			return shape;
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
		double meetsLength = meets.getLength();
		
		
		if (getTurnShape() == TurnShape.G) {
			double parLength = par.getLength();
			double meetsExtension = getExtender().getSlideable().minimumDistanceTo(getMeets().getSlideable());
			int distance = (int) Math.max(0, parLength + meetsExtension - meetsLength);
			return distance * tp.getCostFactor();
			
		} else {
			double parLength = par.getLength();
			int distance = (int) Math.max(0,parLength - meetsLength);
			return distance * tp.getCostFactor();
		}
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
		
		if (meets.getTurnPriority() == TurnPriority.CONNECTION) {
			return TurnType.CONNECTION_NORMAL;
		} else if (meets.getTurnPriority() == TurnPriority.MAXIMIZE_RECTANGULAR) {
			return TurnType.EXTEND_PREFERRED;
		} else {
			// trying to minimize in some way.
			
			if (getTurnShape() == TurnShape.U) {
				return getUShapedTypes(meets, getLink().getTurnPriority(), getPost().getTurnPriority());
			} else {
				return getGShapedTypes(meets, getLink().getTurnPriority(), getPost().getTurnPriority());
			}
		}
	}
	
	
	
	private TurnType getUShapedTypes(VertexTurn meets, TurnPriority link, TurnPriority post) {
		switch (link) {
		case CONNECTION:
			if (meets.getLeavingConnections().size() == 1) {
				return TurnType.MINIMIZE_RECT_OUTSIDE_CORNER_SINGLE;
			} else {
				return TurnType.MINIMIZE_RECT_OUTSIDE_CORNER;
			}
		case MAXIMIZE_RECTANGULAR:
		case MINIMIZE_RECTANGULAR:
		default:
			throw new LogicException();
		}
	}

	private TurnType getGShapedTypes(VertexTurn meets, TurnPriority link, TurnPriority post) {
		switch (link) {
		case CONNECTION:
			if (post == TurnPriority.CONNECTION) {
				return TurnType.MINIMIZE_RECT_SIDE_PART;
			} else if (post == TurnPriority.MINIMIZE_RECTANGULAR) {
				return TurnType.MINIMIZE_RECT_INSIDE_CORNER;
			} else {
				throw new LogicException();
			}
		case MINIMIZE_RECTANGULAR:
			return TurnType.MINIMIZE_RECT_INSIDE_CORNER;
		case MAXIMIZE_RECTANGULAR:
		default:
			return TurnType.EXTEND_PREFERRED;
		}
	}

	
	public TurnShape getTurnShape() {
		return getPost().getDirection() == getExtender().getDirection() ? TurnShape.U : TurnShape.G;
	}
	
	public String toString() {
		return "\n[RO: "+i+"("+this.getInitialScore()+")"+ ", meetsType = "+type+ ", extender = " + getExtender().getSegment() +"]"; 
	}
}
