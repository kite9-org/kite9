package org.kite9.diagram.visualization.compaction.rect;

import java.util.List;

import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.compaction.AbstractCompactionStep;
import org.kite9.diagram.visualization.compaction.rect.PrioritizingRectangularizer.Match;
import org.kite9.diagram.visualization.compaction.rect.VertexTurn.TurnPriority;
import org.kite9.diagram.visualization.compaction.segment.Segment;
import org.kite9.diagram.visualization.compaction.slideable.SegmentSlackOptimisation;
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
		
		CONNECTION_FAN(-10000, null, false, TurnPriority.CONNECTION),
		EXTEND_PREFERRED(0, null, false, TurnPriority.MAXIMIZE_RECTANGULAR),

		MINIMIZE_RECT_SIDE_PART_G(20000, TurnShape.G, false, TurnPriority.MINIMIZE_RECTANGULAR),    // lines up connecteds joining to a connection

		CONNECTION_NORMAL_G(40000, TurnShape.G, false, TurnPriority.CONNECTION),
		//CONNECTION_OTHERSIDE_G(40000, TurnShape.G, true, TurnPriority.CONNECTION),
		CONNECTION_NORMAL_U(50000, TurnShape.U, false, TurnPriority.CONNECTION),
		CONNECTION_OTHERSIDE_U(50000, TurnShape.U, true, TurnPriority.CONNECTION),
		
		MINIMIZE_RECT_NORMAL_G(60000, TurnShape.G, false, TurnPriority.MINIMIZE_RECTANGULAR),
		//MINIMIZE_RECT_OTHERSIDE_G(60000, TurnShape.G, true, TurnPriority.MINIMIZE_RECTANGULAR),
		MINIMIZE_RECT_NORMAL_U(60000, TurnShape.U, false, TurnPriority.MINIMIZE_RECTANGULAR),
		MINIMIZE_RECT_OTHERSIDE_U(60000, TurnShape.U, true, TurnPriority.MINIMIZE_RECTANGULAR);

		private TurnType(int c, TurnShape shape, boolean otherSide, TurnPriority meetsTurnPriority) {
			this.cost = c;
			this.shape = shape;
			this.otherSide = otherSide;
			this.meetsTurnPriority = meetsTurnPriority;
		}
		
		private final int cost;
		private final TurnShape shape;
		private boolean otherSide;
		private TurnPriority meetsTurnPriority;

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
		VertexTurn link = getLink();
		
		TurnPriority tp = meets.calculateTurnPriority();
		double meetsLength = getLength(meets, link, type.useOtherSide());
		
		if (getTurnShape() == TurnShape.G) {
			double parLength = getLength(par, link, type.useOtherSide());
			double meetsExtension = 0;
			meetsExtension = acs.getMinimumDistance(getPost().getSegment(), getExtender().getSegment(), getMeets().getSegment(), true);
			int distance = (int) Math.max(0, parLength + meetsExtension - meetsLength);
			return distance * tp.getCostFactor();
			
		} else {
			double parLength = getLength(par, link, type.useOtherSide());
			int distance = (int) Math.max(0,parLength - meetsLength);
			return distance * tp.getCostFactor();
		}
	}
	
	private double getLength(VertexTurn of, VertexTurn to, boolean otherSide) {
		Slideable<Segment> toSlideable = to.getSlideable();
		Slideable<Segment> early = of.getEarly();
		Slideable<Segment> late = of.getLate();
		
		if (otherSide) {
			SegmentSlackOptimisation sso = (SegmentSlackOptimisation) toSlideable.getSlackOptimisation();
			Connected c = toSlideable.getUnderlying().getRectangulars().stream()
					.filter(r -> r instanceof Connected)
					.map(r -> (Connected) r)
					.findFirst().orElse(null);
			
			if (c == null) {
				throw new LogicException();
			}
			
			OPair<Slideable<Segment>> pair = sso.getSlideablesFor(c);
			if (pair.oneOf(early)) {
				early = pair.otherOne(early);
			} else if (pair.oneOf(late)) {
				late = pair.otherOne(late);
			} else {
				throw new LogicException();
			}
		}
		
		return early.minimumDistanceTo(late);
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
			return getUShapedTypes(meets, getLink());
		} else {
			return getGShapedTypes(meets, getLink().getTurnPriority(), getPar().getTurnPriority(), getPost().getTurnPriority());
		}
	}
	
	
	
	private TurnType getUShapedTypes(VertexTurn meetsTurn, VertexTurn linkTurn) {
		TurnPriority meetsPriority = meetsTurn.getTurnPriority();
		if (meetsPriority == TurnPriority.MAXIMIZE_RECTANGULAR) {
			return TurnType.EXTEND_PREFERRED;
		} else if (meetsPriority == TurnPriority.MINIMIZE_RECTANGULAR) {
			if (linkTurn.isOtherSide()) {
				return TurnType.MINIMIZE_RECT_OTHERSIDE_U;
			} else {
				return TurnType.MINIMIZE_RECT_NORMAL_U;
			}
		} else if (meetsPriority == TurnPriority.CONNECTION) {
			if (linkTurn.isOtherSide()) {
				return TurnType.CONNECTION_OTHERSIDE_U;
			} else {
				return TurnType.CONNECTION_NORMAL_U;
			}
		}
		
		throw new LogicException();
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
				} else {
					return TurnType.MINIMIZE_RECT_NORMAL_G;
				}
			case MINIMIZE_RECTANGULAR:
				return TurnType.MINIMIZE_RECT_NORMAL_G;
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
