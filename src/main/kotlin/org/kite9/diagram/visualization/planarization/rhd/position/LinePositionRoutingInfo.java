package org.kite9.diagram.visualization.planarization.rhd.position;

import org.kite9.diagram.visualization.planarization.mgt.router.LineRoutingInfo;
import org.kite9.diagram.visualization.planarization.mgt.router.RoutableReader.Routing;

public class LinePositionRoutingInfo implements LineRoutingInfo {

	private double runningCostX, runningCostY;

	public void setRunningCostX(double runningCostX) {
		this.runningCostX = runningCostX;
	}

	public void setRunningCostY(double runningCostY) {
		this.runningCostY = runningCostY;
	}

	BoundsBasedPositionRoutingInfo position;
	
	public BoundsBasedPositionRoutingInfo getPositionForTesting() {
		return position;
	}

	BoundsBasedPositionRoutingInfo obstacle;
	Routing r;
	
	public LinePositionRoutingInfo(LinePositionRoutingInfo from, BoundsBasedPositionRoutingInfo pri, Routing r) {
		if (from==null) {
			this.position = pri;
		} else {
			this.runningCostX = from.runningCostX;
			this.runningCostY = from.runningCostY;
			this.position = from.position;
			
			if (from.obstacle != null) {
				avoid(from.r, from.obstacle, pri);
			} 
			
			if (r == null) {
				avoid(r, pri, null);
			} else {
				this.obstacle = pri;
				this.r = r;
			}
		}
	}

	private void avoid(Routing r, BoundsBasedPositionRoutingInfo obstacle, BoundsBasedPositionRoutingInfo next) {
		BoundsBasedPositionRoutingInfo frr = this.position;
		Corner c = getCorner(r, obstacle, next);
		this.position = c.operate(frr, obstacle);	
		this.runningCostX += xCost(frr, this.position);
		this.runningCostY += yCost(frr, this.position);
	}

	private Corner getCorner(Routing r, BoundsBasedPositionRoutingInfo ob, BoundsBasedPositionRoutingInfo next) {
		Corner c = Corner.FINISH;
		if (r != null) {
			return ob.getAvoidanceCorners().get(r);
		}
		return c;
	}
	
	
	

	@Override
	public double getHorizontalRunningCost() {
		return runningCostX;
	}

	@Override
	public double getVerticalRunningCost() {
		return runningCostY;
	}

	@Override
	public double getRunningCost() {
		return runningCostX+runningCostY;
	}

	@Override
	public String toString() {
		return "[x="+position.x+", y="+position.y+", c="+PositionRoutingInfo.nf.format(getRunningCost())+"]";

	}

	private double xCost(BoundsBasedPositionRoutingInfo a, BoundsBasedPositionRoutingInfo b) {
		double xCost = cost(a.getMinX(), a.getMaxX(), b.getMinX(), b.getMaxX());		
		return xCost;
	}
	
	private double yCost(BoundsBasedPositionRoutingInfo a, BoundsBasedPositionRoutingInfo b) {
		double yCost = cost(a.getMinY(), a.getMaxY(), b.getMinY(), b.getMaxY());
		return yCost;
	}
	
	private double cost(double ps, double pe, double as, double ae) {
		if ((pe < as) && (pe < ae)) {
			return Math.abs(pe - Math.min(as, ae));
		} else if ((ps > as) && (ps > ae)) {
			return Math.abs(ps - Math.max(as, ae));			
		} else {
			return 0;
		}
	}
	

	
}
