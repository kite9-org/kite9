package org.kite9.diagram.model;

/**
 * Describes what's at the end of a {@link Connection}.
 * 
 * @author robmoffat
 *
 */
public interface Terminator extends Rectangular {

	/**
	 * Amount of length along the axis of the link that the terminator will take up.
	 */
	double getReservedLength();
	
	/**
	 * Amount of space either side of the terminator that must be reserved so it doesn't collide with
	 * other terminators.
	 */
	double getMargin();
		
}
