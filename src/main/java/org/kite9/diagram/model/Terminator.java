package org.kite9.diagram.model;

/**
 * Describes what's at the end of a {@link Connection}.
 * 
 * @author robmoffat
 *
 */
public interface Terminator extends Rectangular {


	double getReservedLength();
	
	double getMargin();
		
	String getMarkerUrl();
}
