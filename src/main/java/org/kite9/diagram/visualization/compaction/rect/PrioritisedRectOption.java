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
		
		CONNECTION_FAN(-10000, null, false, TurnPriority.CONNECTION, false),
		EXTEND_PREFERRED(0, null, false, TurnPriority.MAXIMIZE_RECTANGULAR, false),

		MINIMIZE_RECT_SIDE_PART_G(20000, TurnShape.G, false, TurnPriority.MINIMIZE_RECTANGULAR, false),    // lines up connecteds joining to a connection
		MINIMIZE_RECT_NORMAL_U_KNOWN(60000, TurnShape.U, false, TurnPriority.MINIMIZE_RECTANGULAR, true),	// known lengths
		MINIMIZE_RECT_NORMAL_G_KNOWN(60000, TurnShape.G, false, TurnPriority.MINIMIZE_RECTANGULAR, true),	// known lengths
		
		CONNECTION_NORMAL_G(40000, TurnShape.G, false, TurnPriority.CONNECTION, false),
		CONNECTION_NORMAL_U(50000, TurnShape.U, false, TurnPriority.CONNECTION, false),
		
		MINIMIZE_RECT_NORMAL_G(60000, TurnShape.G, false, TurnPriority.MINIMIZE_RECTANGULAR, false),
		MINIMIZE_RECT_NORMAL_U(60000, TurnShape.U, false, TurnPriority.MINIMIZE_RECTANGULAR, false),
		;

		private TurnType(int c, TurnShape shape, boolean otherSide, TurnPriority meetsTurnPriority, boolean parKnown) {
			this.cost = c;
			this.shape = shape;
			this.otherSide = otherSide;
			this.meetsTurnPriority = meetsTurnPriority;
			this.parKnown = parKnown;
		}
		
		private final int cost;
		private final TurnShape shape;
		private boolean otherSide;
		private TurnPriority meetsTurnPriority;
		private boolean parKnown;

		public int getCost() {
			return cost;
		}
		
		public TurnShape getTurnShape() {
			return shape;
		}
		
		/**
		 * Indicates we should score push-out using the far-side of the Link VertexTurn, rather 
		 * than near-side.
		 */
		public boolean useOtherSide() {
			return otherSide;
		}
		
		public TurnPriority getMeetsTurnPriority() {
			return meetsTurnPriority;
		}
		
		public boolean parKnown() {
			return parKnown;
		}
	};

	private TurnType type;
	private final AbstractCompactionStep acs;
	
	public TurnType getType() {
		return type;
	}

	public PrioritisedRectOption(int i, VertexTurn vt1, VertexTurn vt2, VertexTurn vt3, VertexTurn vt4, VertexTurn vt5, Match m, List<VertexTurn> fromStack, AbstractCompactionStep in) {
		super(i, vt1, vt2, vt3, vt4, vt5, m, fromStack);
		this.type = calculateType();
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
	
	
	
	@Override
	public void rescore() {
		this.type = calculateType();
		super.rescore();
	}

	private int calculatePushOut() {
		VertexTurn par = getPar();
		VertexTurn meets = getMeets();
		
		TurnPriority tp = meets.getTurnPriority();
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
	
	

	public TurnType calculateType() {
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
			return getUShapedTypes(meets, getLink(), getPar());
		} else {
			return getGShapedTypes(meets, getLink(), getPar(), getPost().getTurnPriority());
		}
	}
	
	
	
	private TurnType getUShapedTypes(VertexTurn meetsTurn, VertexTurn linkTurn, VertexTurn parTurn) {
		TurnPriority meetsPriority = meetsTurn.getTurnPriority();
		if (meetsPriority == TurnPriority.MAXIMIZE_RECTANGULAR) {
			return TurnType.EXTEND_PREFERRED;
		} else if (meetsPriority == TurnPriority.MINIMIZE_RECTANGULAR) {
			if (parTurn.isFixedLength()) {
				return TurnType.MINIMIZE_RECT_NORMAL_U_KNOWN;
			} else {
				return TurnType.MINIMIZE_RECT_NORMAL_U;
			}
		} else if (meetsPriority == TurnPriority.CONNECTION) {
			return TurnType.CONNECTION_NORMAL_U;
		}
		
		throw new LogicException();
	}

	private TurnType getGShapedTypes(VertexTurn meets, VertexTurn link, VertexTurn par, TurnPriority post) {
		TurnPriority meetsTurnPriority = meets.getTurnPriority();
		if (meetsTurnPriority == TurnPriority.CONNECTION) {
			return getNormalGType(par);
		} else if (meetsTurnPriority == TurnPriority.MAXIMIZE_RECTANGULAR) {
			return TurnType.EXTEND_PREFERRED;
		} else if (meetsTurnPriority == TurnPriority.MINIMIZE_RECTANGULAR) {
			switch (link.getTurnPriority()) {
			case CONNECTION:
				if (post == TurnPriority.CONNECTION) {
					return TurnType.MINIMIZE_RECT_SIDE_PART_G;
				} else {
					return getNormalGType(par);
				}
			case MINIMIZE_RECTANGULAR:
				return getNormalGType(par);
			case MAXIMIZE_RECTANGULAR:
			default:
				throw new LogicException();
			}
		} else {
			throw new LogicException();
		}
	}

	private TurnType getNormalGType(VertexTurn par) {
		if (par.isFixedLength()) {
			return TurnType.MINIMIZE_RECT_NORMAL_G_KNOWN;
		} else {
			return TurnType.MINIMIZE_RECT_NORMAL_G;
		}
	}

	
	public TurnShape getTurnShape() {
		return getPost().getDirection() == getExtender().getDirection() ? TurnShape.U : TurnShape.G;
	}
	
	public String toString() {
		return "\n[RO: "+i+"("+this.getInitialScore()+")"+ ", meetsType = "+type+ ", extender = " + getExtender().getSegment() +"]"; 
	}
}
