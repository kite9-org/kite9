package org.kite9.diagram.visualization.planarization.rhd.layout;

/**
 * The placement approach positions the attr and then works out how much the placement 'costs'
 * in terms of the remaining connections.
 * 
 * @author robmoffat
 *
 */
public interface PlacementApproach {

	public void evaluate();
	
	public double getScore();
	
	public void choose();
	
	/** 
	 * Means that the placement order is the same as the numerical ordering of the group ordinals
	 */
	public boolean isNatural();
}