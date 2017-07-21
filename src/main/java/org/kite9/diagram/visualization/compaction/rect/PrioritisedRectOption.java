package org.kite9.diagram.visualization.compaction.rect;

import java.util.List;

import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.compaction.AbstractCompactionStep;
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
		EXTEND_PREFERRED(0, null),

		CONNECTION_NORMAL_G(40000,TurnShape.G),
		CONNECTION_SYMMETRIC_U(40000, TurnShape.U),
		CONNECTION_ASYMMETRIC_U(40000, TurnShape.U),

		MINIMIZE_RECT_INSIDE_CORNER_G(60000, TurnShape.G),		// connection-to-corner of rectangular
		MINIMIZE_RECT_SIDE_PART_G(20000, TurnShape.G),    // connection-to-connection of rectangular 
		MINIMIZE_RECT_OUTSIDE_CORNER_U(60000, TurnShape.U),		
		MINIMIZE_RECT_OUTSIDE_CORNER_SINGLE_U(60000, TurnShape.U),
		MINIMIZE_RECT_ASYMMETRIC_U(60000, TurnShape.U),
		MINIMIZE_RECT_SYMMETRIC_U(60000, TurnShape.U);

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

	private final TurnType type;
	private final AbstractCompactionStep acs;
	
	public TurnType getType() {
		return type;
	}

	public PrioritisedRectOption(int i, VertexTurn vt1, VertexTurn vt2, VertexTurn vt3, VertexTurn vt4, VertexTurn vt5, Match m, List<VertexTurn> fromStack, AbstractCompactionStep in) {
		super(i, vt1, vt2, vt3, vt4, vt5, m, fromStack);
		this.type = initializeType();
		this.acs = in;
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
		double meetsLength = meets.getLength(true);
		
		if (getTurnShape() == TurnShape.G) {
			double parLength = par.getLength(true);
			double meetsExtension = 0;
			meetsExtension = acs.getMinimumDistance(getPost().getSegment(), getExtender().getSegment(), getMeets().getSegment(), true);
			int distance = (int) Math.max(0, parLength + meetsExtension - meetsLength);
			return distance * tp.getCostFactor();
			
		} else {
			double parLength = par.getLength(true);
			int distance = (int) Math.max(0,parLength - meetsLength);
			return distance * tp.getCostFactor();
		}
	}

	private TurnType initializeType() {
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
		
		if (meets.getTurnPriority() == TurnPriority.MAXIMIZE_RECTANGULAR) {
			return TurnType.EXTEND_PREFERRED;
		}
		
		if (getTurnShape() == TurnShape.U) {
			return getUShapedTypes(meets, getLink().getTurnPriority(), getPar().getTurnPriority(), getPost().getTurnPriority());
		} else {
			return getGShapedTypes(meets, getLink().getTurnPriority(), getPar().getTurnPriority(), getPost().getTurnPriority());
		}
	}
	
	
	
	private TurnType getUShapedTypes(VertexTurn meetsTurn, TurnPriority linkPriority, TurnPriority parPriority, TurnPriority postPriority) {
		TurnPriority meetsPriority = meetsTurn.getTurnPriority();
		if (linkPriority == TurnPriority.MINIMIZE_RECTANGULAR) {
			
			// deal with special cases which are symmetrical, and therefore usually higher priority.
			if (meetsPriority == parPriority) {
				if (meetsPriority == TurnPriority.CONNECTION) {
					return TurnType.CONNECTION_SYMMETRIC_U;
				} else if (meetsPriority == TurnPriority.MINIMIZE_RECTANGULAR) {					
					return TurnType.MINIMIZE_RECT_SYMMETRIC_U;
				}
			} 
			
			if (meetsPriority == TurnPriority.CONNECTION) {
				return TurnType.CONNECTION_ASYMMETRIC_U;
			} else if (meetsPriority == TurnPriority.MINIMIZE_RECTANGULAR) {
				return TurnType.MINIMIZE_RECT_ASYMMETRIC_U;
			} else {
				throw new LogicException();
			}
			
		} else if (linkPriority == TurnPriority.CONNECTION) {
			if (meetsPriority == TurnPriority.CONNECTION) {
				return TurnType.CONNECTION_ASYMMETRIC_U;
			} else if (meetsPriority == TurnPriority.MINIMIZE_RECTANGULAR) {
				if (meetsTurn.getLeavingConnections().size() == 1) {
					return TurnType.MINIMIZE_RECT_OUTSIDE_CORNER_SINGLE_U;
				} else {
					return TurnType.MINIMIZE_RECT_OUTSIDE_CORNER_U;
				}
			} else {
				throw new LogicException();
			}
		} else {
			throw new LogicException();
		}
	}

	private TurnType getGShapedTypes(VertexTurn meets, TurnPriority link, TurnPriority par, TurnPriority post) {
		TurnPriority meetsTurnPriority = meets.getTurnPriority();
		if (meetsTurnPriority == TurnPriority.CONNECTION) {
			return TurnType.CONNECTION_NORMAL_G;
		} else if (meetsTurnPriority == TurnPriority.MAXIMIZE_RECTANGULAR) {
			return TurnType.EXTEND_PREFERRED;
		} else if (meetsTurnPriority == TurnPriority.MINIMIZE_RECTANGULAR) {
			switch (link) {
			case CONNECTION:
				if (post == TurnPriority.CONNECTION) {
					return TurnType.MINIMIZE_RECT_SIDE_PART_G;
				} else if (post == TurnPriority.MINIMIZE_RECTANGULAR) {
					return TurnType.MINIMIZE_RECT_INSIDE_CORNER_G;
				} else {
					throw new LogicException();
				}
			case MINIMIZE_RECTANGULAR:
				return TurnType.MINIMIZE_RECT_INSIDE_CORNER_G;
			case MAXIMIZE_RECTANGULAR:
			default:
				throw new LogicException();
			}
		} else {
			throw new LogicException();
		}
	}

	
	public TurnShape getTurnShape() {
		return getPost().getDirection() == getExtender().getDirection() ? TurnShape.U : TurnShape.G;
	}
	
	public String toString() {
		return "\n[RO: "+i+"("+this.getInitialScore()+")"+ ", meetsType = "+type+ ", extender = " + getExtender().getSegment() +"]"; 
	}
}
