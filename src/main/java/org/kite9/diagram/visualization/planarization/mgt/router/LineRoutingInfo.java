package org.kite9.diagram.visualization.planarization.mgt.router;


/**
 * Line Routing objects are immutable.  They represent a route through the planarization
 */
public interface LineRoutingInfo {
	
	public double getHorizontalRunningCost();
	
	public double getVerticalRunningCost();
	
	public double getRunningCost();
	
}
