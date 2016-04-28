package org.kite9.diagram.visualization.planarization.rhd.position;

import org.kite9.diagram.common.elements.RoutingInfo;
import org.kite9.diagram.visualization.planarization.mgt.router.RoutableReader;

/**
 * 
 * A simple manhattan distance metric is used to identify the cost of the arrangement,
 * so each RoutingInfo is based on position.  This class handles pretty much all of the cost arrangements and 
 * distance calculations.
 * 
 */
public abstract class AbstractPositionRoutableReader implements RoutableReader, RoutableHandler2D {

	public AbstractPositionRoutableReader() {
		super();
	}
	
	public static final PositionRoutingInfo EMPTY_BOUNDS = new PositionRoutingInfo() {
		
		@Override
		public double centerY() { return 0;}
		
		@Override
		public double centerX() { return 0;}
		
		@Override
		public double getWidth() {return 0;}
		
		@Override
		public double getMinY() {return 0;}
		
		@Override
		public double getMinX() {return 0;}
		
		@Override
		public double getMaxY() {return 0;}
		
		@Override
		public double getMaxX() {return 0;}
		
		@Override
		public double getHeight() {return 0;}

		@Override
		public int compareTo(RoutingInfo arg0) {
			throw new UnsupportedOperationException("Can't compare empty bounds");
		}

		@Override
		public boolean isBreakingOrder() {
			return false;
		}

		@Override
		public int compareX(RoutingInfo with) {
			throw new UnsupportedOperationException("Can't compare empty bounds");
		}

		@Override
		public int compareY(RoutingInfo with) {
			throw new UnsupportedOperationException("Can't compare empty bounds");
		}
	};

	private double minDist(double min1, double max1, double min2, double max2) {
		if (min2 > min1) {
			if (min2 < max1) {
				return 0;
			} else {
				return min2 - max1;
			}
		} else {
			if (max2 > min1) {
				return 0;
			} else {
				return min1 - max2;
			}
		}
	}

	@Override
	public double cost(RoutingInfo from, RoutingInfo to) {
		PositionRoutingInfo fd = (PositionRoutingInfo) from;
		PositionRoutingInfo td = (PositionRoutingInfo) to;
		
		return minDist(fd.getMinX(),fd.getMaxX(), td.getMinX(), td.getMaxX())
		+ minDist(fd.getMinY(), fd.getMaxY(), td.getMinY(), td.getMaxY());
	}
	
	private double[] narrow(double a1, double a2, double b1, double b2) {
		if (a2 < b1) {
			return new double[] { b1, b1, b1-a2};
		} else if (a1 > b2) {
			return new double[] { b2, b2, a1-b2};
		} else {
			return new double[] { Math.max(a1, b1), Math.min(a2, b2), 0};
		}
	}
	

	
	@Override
	public RoutingInfo emptyBounds() {
		return EMPTY_BOUNDS;
	}
	
	@Override
	public boolean isEmptyBounds(RoutingInfo bounds) {
		return bounds == EMPTY_BOUNDS;
	}

	@Override
	public boolean isInPlane(RoutingInfo to, RoutingInfo from,
			boolean horiz) {
		
		PositionRoutingInfo pto = (PositionRoutingInfo) to;
		PositionRoutingInfo pfrom= (PositionRoutingInfo) from;
		
		if (!horiz) {
			boolean within = checkHorizontal(pto, pfrom);
			return within;
		} else {
			boolean within = checkVertical(pto, pfrom);
			return within;
		}
	}

	private boolean checkHorizontal(PositionRoutingInfo pto,
			PositionRoutingInfo pfrom) {
		return narrow(pto.getMinX(), pto.getMaxX(), pfrom.getMinX(), pfrom.getMaxX())[2] == 0;
	}
	
	private boolean checkVertical(PositionRoutingInfo pto,
			PositionRoutingInfo pfrom) {
		return narrow(pto.getMinY(), pto.getMaxY(), pfrom.getMinY(), pfrom.getMaxY())[2] == 0;
	}

}

		
