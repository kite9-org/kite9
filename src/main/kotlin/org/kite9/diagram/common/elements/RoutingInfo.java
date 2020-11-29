package org.kite9.diagram.common.elements;

/**
 * Stores some kind of information about the planarization position of a vertex in order that we 
 * can route edges around it.
 * 
 * @author robmoffat
 *
 */
public interface RoutingInfo extends Comparable<RoutingInfo>{

	public String outputX();
	public String outputY();
	
	public double centerX();
	
	public double centerY();
	
	public int compareX(RoutingInfo with);
	public int compareY(RoutingInfo with);
	

}
