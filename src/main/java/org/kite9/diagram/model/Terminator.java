package org.kite9.diagram.model;

/**
 * Describes what's at the end of a {@link Connection}.
 * 
 * @author robmoffat
 *
 */
public interface Terminator extends DiagramElement {


	double getReservedLength();
	
	double getMargin();
		
}
