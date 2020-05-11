package org.kite9.diagram.visualization.compaction.rect;

import java.util.List;
import java.util.Set;

import org.kite9.diagram.model.Rectangular;
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
	
	static enum GrowthRisk {
		ZERO, LOW, HIGH
	}
	
	static enum TurnType {
		
		CONNECTION_FAN(-100000, TurnPriority.CONNECTION, GrowthRisk.ZERO),
		CONTAINER_LABEL_MAXIMIZE(-30000, TurnPriority.MAXIMIZE_RECTANGULAR, GrowthRisk.ZERO),
		EXTEND_PREFERRED(0, TurnPriority.MAXIMIZE_RECTANGULAR, GrowthRisk.ZERO),

		MINIMIZE_RECT_SIDE_PART_G(20000, TurnPriority.MINIMIZE_RECTANGULAR, GrowthRisk.LOW),    // lines up connecteds joining to a connection

		CONNECTION_ZERO(40000, TurnPriority.CONNECTION, GrowthRisk.ZERO),
		CONNECTION_LOW(50000, TurnPriority.CONNECTION, GrowthRisk.LOW),
		CONNECTION_HIGH(50000, TurnPriority.CONNECTION, GrowthRisk.HIGH),
		
		MINIMIZE_RECT_ZERO(60000, TurnPriority.MINIMIZE_RECTANGULAR, GrowthRisk.ZERO),	
		MINIMIZE_RECT_LOW(60000, TurnPriority.MINIMIZE_RECTANGULAR, GrowthRisk.LOW),	
		MINIMIZE_RECT_HIGH(70000, TurnPriority.MINIMIZE_RECTANGULAR, GrowthRisk.HIGH),	
		
		;

		private TurnType(int c, TurnPriority meetsTurnPriority, GrowthRisk growthLikely) {
			this.cost = c;
			this.meetsTurnPriority = meetsTurnPriority;
			this.growthLikelihood = growthLikely;
		}
		
		private final int cost;
		private TurnPriority meetsTurnPriority;
		private GrowthRisk growthLikelihood;
		

		public int getCost() {
			return cost;
		}
		
		public TurnPriority getMeetsTurnPriority() {
			return meetsTurnPriority;
		}
		
		public GrowthRisk getGrowthLikelihood() {
			return growthLikelihood;
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
		int deduction = getDeduction(pushOut);
		return pushOut + typeCost + deduction;
	}
	
	private int getDeduction(int pushOut) {
		if ((pushOut == 0) && (type.getGrowthLikelihood() == GrowthRisk.ZERO)) {
			return -30000;
		} else {
			return 0;
		}
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
		
		if ((meets.getTurnPriority() == TurnPriority.MAXIMIZE_RECTANGULAR) || (inside(par, meets))) {
			if (par.isContainerLabelOnSide(extender.getDirection())) {
				return TurnType.CONTAINER_LABEL_MAXIMIZE;
			} else {
				return TurnType.EXTEND_PREFERRED;
			}
			
		}
		
		if (getTurnShape() == TurnShape.U) {
			return getUShapedTypes(meets, getLink(), getPar());
		} else {
			return getGShapedTypes(meets, getLink(), getPar(), getPost().getTurnPriority());
		}
	}
	
	
	
	private boolean inside(VertexTurn par, VertexTurn meets) {
		Set<Rectangular> containers = meets.getSegment().getRectangulars();
 		
		long count = par.getSegment().getRectangulars().stream().filter(r -> {
			if (containers.contains(r.getParent())) {
				return true;
			}
			
			return false;
		}).count();
		
		return count > 0;
	}

	private TurnType getUShapedTypes(VertexTurn meetsTurn, VertexTurn linkTurn, VertexTurn parTurn) {
		TurnPriority meetsPriority = meetsTurn.getTurnPriority();
		if (meetsPriority == TurnPriority.MAXIMIZE_RECTANGULAR) {
			return TurnType.EXTEND_PREFERRED;
		} else if (meetsPriority == TurnPriority.MINIMIZE_RECTANGULAR) {
			return getMinimizeTurnType(parTurn);
		} else if (meetsPriority == TurnPriority.CONNECTION) {
			return getConnectionTurnType(parTurn);
		}
		
		throw new LogicException();
	}

	private TurnType getMinimizeTurnType(VertexTurn parTurn) {
		if (parTurn.isNonExpandingLength()) {
			return TurnType.MINIMIZE_RECT_ZERO;
		} else if (parTurn.getTurnPriority() == TurnPriority.MINIMIZE_RECTANGULAR) {
			return TurnType.MINIMIZE_RECT_LOW;
		} else {
			return TurnType.MINIMIZE_RECT_HIGH;
		}
	}

	private TurnType getConnectionTurnType(VertexTurn parTurn) {
		if (parTurn.isNonExpandingLength()) {
			return TurnType.CONNECTION_ZERO;
		} else if (parTurn.getTurnPriority() != TurnPriority.MAXIMIZE_RECTANGULAR) {
			return TurnType.CONNECTION_LOW;
		} else {
			return TurnType.CONNECTION_HIGH;
		}
	}

	private TurnType getGShapedTypes(VertexTurn meets, VertexTurn link, VertexTurn par, TurnPriority post) {
		TurnPriority meetsTurnPriority = meets.getTurnPriority();
		if (meetsTurnPriority == TurnPriority.CONNECTION) {
			return getConnectionTurnType(par);
		} else if (meetsTurnPriority == TurnPriority.MAXIMIZE_RECTANGULAR) {
			return TurnType.EXTEND_PREFERRED;
		} else if (meetsTurnPriority == TurnPriority.MINIMIZE_RECTANGULAR) {
			switch (link.getTurnPriority()) {
			case CONNECTION:
				if (post == TurnPriority.CONNECTION) {
					return TurnType.MINIMIZE_RECT_SIDE_PART_G;
				} else {
					return getMinimizeTurnType(par);
				}
			case MINIMIZE_RECTANGULAR:
				return getMinimizeTurnType(par);
			case MAXIMIZE_RECTANGULAR:
			default:
				// can be for keys, apparently
				return TurnType.EXTEND_PREFERRED;
				
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
