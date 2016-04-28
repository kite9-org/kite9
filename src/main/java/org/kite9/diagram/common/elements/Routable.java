package org.kite9.diagram.common.elements;



/**
 * The {@link SSPEdgeRouter} requires that vertices implement Routeable so that it can route edges 
 * correctly around them.
 * 
  * @author robmoffat
 *
 */
public interface Routable {

	public RoutingInfo getRoutingInfo();
	
	public void setRoutingInfo(RoutingInfo gi);

}
